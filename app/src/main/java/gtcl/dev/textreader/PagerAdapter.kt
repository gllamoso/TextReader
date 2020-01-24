package gtcl.dev.textreader

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import gtcl.dev.textreader.mainfragments.CameraFragment
import gtcl.dev.textreader.mainfragments.StorageFragment

class PagerAdapter(fm: FragmentManager, behavior: Int, val numberOfTabs: Int) : FragmentStatePagerAdapter(fm, behavior) {

    lateinit var cameraFragment: CameraFragment
    lateinit var storageFragment: StorageFragment

    override fun getItem(position: Int): Fragment {
        return when(position){
            MainActivity.CAMERA_FRAGMENT_INDEX -> {
                if(!::cameraFragment.isInitialized)
                    cameraFragment = CameraFragment()
                cameraFragment
            }
            MainActivity.STORAGE_FRAGMENT_INDEX ->{
                if(!::storageFragment.isInitialized)
                    storageFragment = StorageFragment()
                storageFragment
            }
            else -> Fragment()
        }
    }

    override fun getCount(): Int {
        return numberOfTabs
    }

}