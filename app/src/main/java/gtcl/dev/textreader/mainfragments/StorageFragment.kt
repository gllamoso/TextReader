package gtcl.dev.textreader.mainfragments


import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import gtcl.dev.textreader.SettingsActivity
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import gtcl.dev.textreader.R

private const val REQUEST_STORAGE_PERMISSION_ID = 1001
private const val STORAGE_REQUEST_CODE = 400

class StorageFragment : Fragment() {

    private lateinit var mResultEditText: EditText
    private lateinit var mPreviewImageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_storage, container, false)

        mResultEditText = rootView.findViewById(R.id.result_text)
        mPreviewImageView = rootView.findViewById(R.id.image_preview)

        val toolBar = rootView.findViewById<Toolbar>(R.id.toolbar_storage)
        setHasOptionsMenu(true)
        (activity!! as AppCompatActivity).setSupportActionBar(toolBar)
        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.add_image -> if(!hasCameraPermission()) requestStoragePermission() else startGalleryActivity()
            R.id.copy_from_storage -> copyTextToClipboard()
            R.id.share_from_storage -> shareText()
            R.id.settings -> startSettingsActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == RESULT_OK) {
            when(requestCode){
                REQUEST_STORAGE_PERMISSION_ID -> CropImage.activity(data!!.data)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(activity as Activity)
            }
        }

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri = result.uri
                mPreviewImageView.setImageURI(resultUri)

                val bitmapDrawable = mPreviewImageView.drawable as BitmapDrawable
                val bitmap = bitmapDrawable.bitmap

                val recognizer = TextRecognizer.Builder(context).build()
                if (!recognizer.isOperational) {
                    Toast.makeText(context, getText(R.string.dependencies_not_available), Toast.LENGTH_SHORT).show()
                } else {
                    val frame = Frame.Builder().setBitmap(bitmap).build()
                    val items = recognizer.detect(frame)
                    val stringBuilder = StringBuilder()
                    for (i in 0 until items.size()) {
                        val myItem = items.valueAt(i)
                        stringBuilder.append(myItem.value)
                        stringBuilder.append("\n")
                    }
                    if(stringBuilder.isNotEmpty())
                        stringBuilder.deleteCharAt(stringBuilder.lastIndex)
                    mResultEditText.setText(stringBuilder.toString())
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Toast.makeText(context, "" + error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == STORAGE_REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if(hasCameraPermission()){
                    startGalleryActivity()
                }
            }
        }
    }

    // -------------------------HELPER FUNCTIONS-----------------------------------------------

    private fun requestStoragePermission() {
        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            STORAGE_REQUEST_CODE
        )
    }

    private fun startGalleryActivity() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_STORAGE_PERMISSION_ID)
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    fun setText(text: String){
        mResultEditText.setText(text)
        mPreviewImageView.setImageURI(null)
    }

    private fun copyTextToClipboard(){
        if(mResultEditText.text.isEmpty()){
            Toast.makeText(context, getText(R.string.no_text_found), Toast.LENGTH_SHORT).show()
        } else {
            val clipboard = activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(getText(R.string.app_name), mResultEditText.text)
            clipboard.primaryClip = clip

            Toast.makeText(context, getText(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareText(){
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, mResultEditText.text.toString())
            type = "text/plain"
        }
        startActivity(sendIntent)
    }

    private fun startSettingsActivity(){
        val intent = Intent(context, SettingsActivity::class.java)
        startActivity(intent)
    }
}
