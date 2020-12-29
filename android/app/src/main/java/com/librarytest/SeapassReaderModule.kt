package com.librarytest

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.widget.Toast
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.librarytest.utils.Coroutines
import com.rccl.excalibur.seapassreader.domain.model.SeapassCardData
import com.rccl.excalibur.seapassreader.domain.usecase.ReadSeapassCardUseCase
import com.rccl.excalibur.seapassreader.domain.usecase.WriteSeapassCardUseCase
import com.rccl.excalibur.seapassreader.readers.acs.AcsCallbackType
import com.rccl.excalibur.seapassreader.readers.factory.UseCaseFactoryImpl
import com.rccl.excalibur.seapassreader.readers.nfc.ReadNfcAndroidUseCase
import kotlinx.coroutines.Job

class SeapassReaderModule(val context: ReactApplicationContext) : ReactContextBaseJavaModule() {

    private lateinit var readJob: Job
    private lateinit var writeJob: Job
    private lateinit var readSeapassCardUseCase: ReadSeapassCardUseCase
    private lateinit var writeSeapassCardUseCase: WriteSeapassCardUseCase

    private val mActivityEventListener: ActivityEventListener = object : BaseActivityEventListener() {
        override fun onNewIntent(intent: Intent?) {
            super.onNewIntent(intent)
            if (NfcAdapter.ACTION_TECH_DISCOVERED == intent?.action) {
                val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)

                tag?.run {
                    (readSeapassCardUseCase as ReadNfcAndroidUseCase).setTag(tag)
                    onTagDetected()
                }
            }
        }
    }

    init {
        initUseCases()
        context.addActivityEventListener(mActivityEventListener);
    }

    fun initUseCases() {
        UseCaseFactoryImpl()
                .createSeapassCardUseCases(
                        "",
                        "",
                        null,
                        null,
                        ::connectionStatusListener,
                        ::onTagDetected
                ).let { seapassCardUseCases ->
                    readSeapassCardUseCase = seapassCardUseCases.first
                    writeSeapassCardUseCase = seapassCardUseCases.second
                }
    }

    override fun getName(): String {
        return "SeapassReaderModule"
    }

    @ReactMethod
    fun read() {
        readJob = Coroutines.ioThenMain(
                { readSeapassCardUseCase() },
                {
                    val params = Arguments.createMap()
                    params.putString("folioNumber", it?.folioNumber ?: "Not read")
                    params.putString("debarkationDate", it?.debarkationDate ?: "Not read")
                    params.putString("secondaryFolioNumber", it?.secondaryFolioNumber ?: "Not read")
                    params.putString("loyaltyTierCode", it?.loyaltyTierCode ?: "Not read")
                    params.putString("shipCode", it?.shipCode ?: "Not read")
                    params.putString("musterStation", it?.musterStationNumber ?: "Not read")
                    params.putString("embarkationDate", it?.embarkationDate ?: "Not read")
                    params.putString("guestFirstName", it?.guestFirstName ?: "Not read")
                    params.putString("guestMiddleName", it?.guestMiddleName ?: "Not read")
                    params.putString("guestLastName", it?.guestLastName ?: "Not read")
                    sendEvent(context, "read", params)
                    Toast.makeText(context, "Values read correctly", Toast.LENGTH_LONG).show()
                }
        )
    }

    @ReactMethod
    fun write(
            folioNumber: String,
            debarkationDate: String,
            secondaryFolioNumber: String,
            loyaltyTierCode: String,
            shipCode: String,
            musterStation: String,
            embarkationDate: String,
            guestFirstName: String,
            guestMiddleName: String,
            guestLastName: String
    ) {
        val seapassCardData = SeapassCardData(
                folioNumber,
                debarkationDate,
                secondaryFolioNumber,
                loyaltyTierCode,
                shipCode,
                musterStation,
                embarkationDate,
                guestFirstName,
                guestMiddleName,
                guestLastName
        )

        writeJob = Coroutines.ioThenMain(
                { writeSeapassCardUseCase(seapassCardData) },
                {
                    if (it == true) {
                        Toast.makeText(context, "Values write correctly", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "There was a problem writing to the tag", Toast.LENGTH_LONG).show()
                    }
                }
        )
    }

    fun onTagDetected() {
        val params = Arguments.createMap()
        sendEvent(context, "tagdetected", params)
    }

    private fun sendEvent(reactContext: ReactContext, eventName: String, params: WritableMap?) {
        reactContext
                .getJSModule(RCTDeviceEventEmitter::class.java)
                .emit(eventName, params)
    }

    private fun connectionStatusListener(acsCallbackType: AcsCallbackType, resultCode: Int) {

    }
}