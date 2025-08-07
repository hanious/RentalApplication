package com.example.rentalapplication

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.rentalapplication.MainActivity.Companion.CREDIT_VALUE
import com.example.rentalapplication.MainActivity.Companion.ITEM
import com.example.rentalapplication.MainActivity.Companion.ITEM_INDEX
import com.example.rentalapplication.MainActivity.Companion.ITEM_STATE
import com.example.rentalapplication.MainActivity.Companion.REMAIN_CREDIT_VALUE


class CheckoutActivity : AppCompatActivity() {

    // Retrieve the Item object Index from the Intent

    companion object {
        const val DURATION = "DURATION"
    }
    private lateinit var item: Item

    private lateinit var edtDuration: EditText
    private lateinit var txtCredit: TextView
    private var creditValue = 0
    private lateinit var txtRemainCredit: TextView
    private var remainingCredit = 0
    private lateinit var txtTotalValue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_checkout)

        val itemIndex = intent.getIntExtra(ITEM_INDEX, 0)
        // Retrieve the Item object from the Intent
        item = intent.getParcelableExtra(ITEM, Item::class.java)!!
        // Retrieve the current creditValue from the Intent
        creditValue = intent.getIntExtra(CREDIT_VALUE, 0) // Default value is 0
        // Update the UI
        val imgItem = findViewById<ImageView>(R.id.img_item)
        txtCredit = findViewById(R.id.txt_credit)
        txtCredit.text = "$creditValue"
        val txtName = findViewById<TextView>(R.id.txt_name)
        val txtPrice = findViewById<TextView>(R.id.txt_price)
        txtTotalValue = findViewById(R.id.txt_total_value)
        val rtgBar = findViewById<RatingBar>(R.id.rtg_bar)
        rtgBar.setIsIndicator(true)
        val txtColor = findViewById<TextView>(R.id.txt_color_value)
        txtRemainCredit = findViewById(R.id.txt_remain_value)
        edtDuration = findViewById(R.id.edt_duration)
        val btnConfirm = findViewById<Button>(R.id.btn_confirm)
        val btnReturn = findViewById<ImageButton>(R.id.btn_return)

        // Update the UI with the passed Item data
        imgItem.setImageResource(item.imageResId)
        txtName.text = item.name
        rtgBar.rating = item.rating
        txtColor.text = item.attribute
        txtPrice.text = "${item.price}"
        txtTotalValue.text = "${item.price}"
        // Update the UI with the current creditValue
        txtCredit.text = "$creditValue"

        // Display remaining Credit
        txtRemainCredit.text = "${creditValue - item.price}"

        // Update total price based on duration
        edtDuration.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val duration = s.toString().toIntOrNull() ?: 1
                val totalPrice = item.price * duration
                txtTotalValue.text = "$totalPrice"
                txtRemainCredit.text = "${creditValue - totalPrice}"
            }
        })

        // On Order Confirm
        btnConfirm.setOnClickListener {
            if (!item.isBorrowed) {
                item.isBorrowed = true
                if (edtDuration.text.isEmpty()) {
                    Toast.makeText(applicationContext, "Please enter a duration.", Toast.LENGTH_SHORT).show()
                }
                val duration = edtDuration.text.toString().toInt()
                remainingCredit = creditValue - (item.price * duration)
                if (remainingCredit < 0) {
                    Toast.makeText(applicationContext, "Not enough Credit!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "Checkout Confirmed!", Toast.LENGTH_SHORT).show()
                    val intentReturn = Intent().apply {
                    putExtra(ITEM_INDEX, itemIndex)
                    putExtra(ITEM_STATE, item.isBorrowed)
                    putExtra(REMAIN_CREDIT_VALUE, remainingCredit)
                }
                    setResult(RESULT_OK, intentReturn)
                    finish()}
            }
        }

        // Handle "Return" button click -> change activity
        btnReturn.setOnClickListener {
            Toast.makeText(applicationContext, "Checkout Cancelled!", Toast.LENGTH_SHORT).show()
            remainingCredit = creditValue
            val intentReturn = Intent().apply {
                putExtra(ITEM_INDEX, itemIndex)
                putExtra(ITEM_STATE, item.isBorrowed)
                putExtra(REMAIN_CREDIT_VALUE, remainingCredit)
            }
            setResult(RESULT_OK, intentReturn)
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val itemIndex = intent.getIntExtra(ITEM_INDEX, 0)
        outState.putInt(ITEM_INDEX, itemIndex)
        outState.putInt(CREDIT_VALUE, creditValue)
        outState.putInt(REMAIN_CREDIT_VALUE, remainingCredit)
        outState.putString(DURATION, edtDuration.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        creditValue = savedInstanceState.getInt(CREDIT_VALUE, 0)
        remainingCredit = savedInstanceState.getInt(REMAIN_CREDIT_VALUE, 0)
        val duration = savedInstanceState.getString(DURATION, "")
        edtDuration.setText(duration)
        txtCredit.text = "$creditValue"
        txtRemainCredit.text = "$remainingCredit"
        val totalPrice = item.price * (duration.toIntOrNull() ?: 1)
        txtTotalValue.text = "$totalPrice"
    }

}