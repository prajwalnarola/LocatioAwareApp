import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.locationawareapp.UserRepository
import com.example.locationawareapp.model.ResponseSuccess
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.RequestBody

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _responseSuccess = MutableLiveData<ResponseSuccess>()
    val responseSuccess: LiveData<ResponseSuccess> get() = _responseSuccess

    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> get() = _error

    fun addLocationData(
        location: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody,
        locationType: RequestBody
    ) {
        val disposable = userRepository.addLocationData(location, latitude, longitude, locationType)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                _responseSuccess.postValue(response)
            }, { throwable ->
                _error.postValue(throwable)
            })

        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
