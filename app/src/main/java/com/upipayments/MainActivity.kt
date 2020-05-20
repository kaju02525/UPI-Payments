package com.upipayments

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    var amountEt: EditText? = null
    var noteEt: EditText? = null
    var nameEt: EditText? = null
    var upiIdEt: EditText? = null
    var send: Button? = null
    val UPI_PAYMENT = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeViews()
        send!!.setOnClickListener { //Getting the values from the EditTexts
            val amount = amountEt!!.text.toString()
            val note = noteEt!!.text.toString()
            val name = nameEt!!.text.toString()
            val upiId = upiIdEt!!.text.toString()
            payUsingUpi(amount, upiId, name, note)
        }
    }

    fun initializeViews() {
        send = findViewById(R.id.send)
        amountEt = findViewById(R.id.amount_et)
        noteEt = findViewById(R.id.note)
        nameEt = findViewById(R.id.name)
        upiIdEt = findViewById(R.id.upi_id)
    }

    private fun payUsingUpi(
            amount: String?,
            upiId: String?,
            name: String?,
            note: String?
    ) {
        val uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", name)
                .appendQueryParameter("tn", note)
                .appendQueryParameter("am", amount)
                .appendQueryParameter("cu", "INR")
                .build()
        val upiPayIntent = Intent(Intent.ACTION_VIEW)
        upiPayIntent.data = uri

        // will always show a dialog to user to choose an app
        val chooser = Intent.createChooser(upiPayIntent, "Pay with")

        // check if intent resolves
        if (null != chooser.resolveActivity(getPackageManager())) {
            startActivityForResult(chooser, UPI_PAYMENT)
        } else {
            Toast.makeText(
                    this@MainActivity,
                    "No UPI app found, please install one to continue",
                    Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            UPI_PAYMENT -> if (RESULT_OK === resultCode || resultCode == 11) {
                if (data != null) {
                    val trxt = data.getStringExtra("response")
                    Log.d("UPI", "onActivityResult: $trxt")
                    val dataList =
                            ArrayList<String>()
                    dataList.add(trxt)
                    upiPaymentDataOperation(dataList)
                } else {
                    Log.d("UPI", "onActivityResult: " + "Return data is null")
                    val dataList =
                            ArrayList<String>()
                    dataList.add("nothing")
                    upiPaymentDataOperation(dataList)
                }
            } else {
                Log.d(
                        "UPI",
                        "onActivityResult: " + "Return data is null"
                ) //when user simply back without payment
                val dataList =
                        ArrayList<String>()
                dataList.add("nothing")
                upiPaymentDataOperation(dataList)
            }
        }
    }

    private fun upiPaymentDataOperation(data: ArrayList<String>) {
        if (isConnectionAvailable(this@MainActivity)) {
            var str = data[0]
            Log.d("UPIPAY", "upiPaymentDataOperation: $str")
            var paymentCancel = ""
            if (str == null) str = "discard"
            var status = ""
            var approvalRefNo = ""
            val response = str.split("&").toTypedArray()
            for (i in response.indices) {
                val equalStr =
                        response[i].split("=").toTypedArray()
                if (equalStr.size >= 2) {
                    if (equalStr[0].toLowerCase() == "Status".toLowerCase()) {
                        status = equalStr[1].toLowerCase()
                    } else if (equalStr[0]
                                    .toLowerCase() == "ApprovalRefNo".toLowerCase() || equalStr[0]
                                    .toLowerCase() == "txnRef".toLowerCase()
                    ) {
                        approvalRefNo = equalStr[1]
                    }
                } else {
                    paymentCancel = "Payment cancelled by user."
                }
            }
            if (status == "success") {
                //Code to handle successful transaction here.
                Toast.makeText(this@MainActivity, "Transaction successful.", Toast.LENGTH_SHORT)
                        .show()
                Log.d("UPI", "responseStr: $approvalRefNo")
            } else if ("Payment cancelled by user." == paymentCancel) {
                Toast.makeText(this@MainActivity, "Payment cancelled by user.", Toast.LENGTH_SHORT)
                        .show()
            } else {
                Toast.makeText(
                        this@MainActivity,
                        "Transaction failed.Please try again",
                        Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                    this@MainActivity,
                    "Internet connection is not available. Please check and try again",
                    Toast.LENGTH_SHORT
            ).show()
        }
    }

    companion object {
        fun isConnectionAvailable(context: Context): Boolean {
            val connectivityManager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivityManager != null) {
                val netInfo = connectivityManager.activeNetworkInfo
                if (netInfo != null && netInfo.isConnected
                        && netInfo.isConnectedOrConnecting
                        && netInfo.isAvailable
                ) {
                    return true
                }
            }
            return false
        }
    }
}