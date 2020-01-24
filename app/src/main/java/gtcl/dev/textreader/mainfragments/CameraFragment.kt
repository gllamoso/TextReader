package gtcl.dev.textreader.mainfragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.Fragment
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import gtcl.dev.textreader.MainActivity
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import gtcl.dev.textreader.R
import java.io.IOException
import java.lang.StringBuilder

const val REQUEST_CAMERA_PERMISSION_ID = 1000

class CameraFragment : Fragment() {
    private lateinit var mCameraView: SurfaceView
    private lateinit var mTextView: TextView
    private lateinit var mCameraSource: CameraSource

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_camera, container, false)

        mCameraView = rootView.findViewById(R.id.surface_view)
        mTextView = rootView.findViewById(R.id.text_view)

        setUpCameraView(rootView)
        setUpButtons(rootView)

        return rootView

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == REQUEST_CAMERA_PERMISSION_ID){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    try{
                        mCameraSource.start(mCameraView.holder)
                    } catch(e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    // ----------------- Helper Functions ----------------------

    private fun setUpCameraView(rootView: View){
        val displayMetrics = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels

        val textRecognizer = TextRecognizer.Builder(rootView.context).build()
        if (!textRecognizer.isOperational)
            Toast.makeText(context, getString(R.string.dependencies_not_available), Toast.LENGTH_LONG).show()
        else {
            mCameraSource = CameraSource.Builder(rootView.context, textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(height, width)
                .setAutoFocusEnabled(true)
                .setRequestedFps(30F)
                .build()
            mCameraView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {}

                override fun surfaceDestroyed(holder: SurfaceHolder?) {
                    mCameraSource.stop()
                }

                override fun surfaceCreated(holder: SurfaceHolder?) {
                    if (ActivityCompat.checkSelfPermission(
                            rootView.context,
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            activity!!,
                            arrayOf(Manifest.permission.CAMERA),
                            REQUEST_CAMERA_PERMISSION_ID
                        )
                        return
                    }
                    mCameraSource.start(mCameraView.holder)
                }
            })

            textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
                override fun release() {}

                override fun receiveDetections(detections: Detector.Detections<TextBlock>?) {
                    val items = detections?.detectedItems ?: return
                    if (items.size() != 0) {
                        mTextView.post {
                            val stringBuilder = StringBuilder()
                            for (i in 0..items.size()) {
                                if (items[i] != null) {
                                    stringBuilder.append(items[i].value)
                                    stringBuilder.append("\n")
                                }
                            }
                            if(stringBuilder.isNotEmpty())
                                stringBuilder.deleteCharAt(stringBuilder.lastIndex)
                            mTextView.text = stringBuilder.toString()
                        }
                    }
                }

            })

        }
    }

    private fun setUpButtons(rootView: View){
        val editFromCamera = rootView.findViewById<ImageView>(R.id.edit_from_camera)
        val copyFromCamera = rootView.findViewById<ImageView>(R.id.copy_from_camera)
        val shareFromCamera = rootView.findViewById<ImageView>(R.id.share_from_camera)

        editFromCamera.setOnClickListener { editText() }
        copyFromCamera.setOnClickListener { copyTextToClipboard() }
        shareFromCamera.setOnClickListener { shareText() }
    }

    private fun copyTextToClipboard(){
        val clipboard = activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(getText(R.string.app_name), mTextView.text)
        clipboard.primaryClip = clip

        Toast.makeText(context, getText(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
    }

    private fun editText(){
        val mainActivity = context as MainActivity
        val storageFragment: StorageFragment = mainActivity.getFragment(MainActivity.STORAGE_FRAGMENT_INDEX) as StorageFragment
        storageFragment.setText(mTextView.text as String)
        mainActivity.selectTab(MainActivity.STORAGE_FRAGMENT_INDEX)
    }

    private fun shareText(){
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, mTextView.text)
            type = "text/plain"
        }
        startActivity(sendIntent)
    }

}
