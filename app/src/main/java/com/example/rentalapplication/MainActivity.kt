package com.example.rentalapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    // List of images
    private val listItem = listOf(
        Item("Keyboard", 4.5f, "Red", 200, R.drawable.item_1),
        Item("Guitar", 5.0f, "Red", 100, R.drawable.item_2),
        Item("Drums", 3.5f, "Red", 75, R.drawable.item_3)
    )

    private var itemIndex = 0 // Default item index
    private var creditValue = 299 //Default user credit balance
    private lateinit var rgrColor: RadioGroup
    private lateinit var imgItem: ImageView
    private lateinit var btnNext: ImageButton
    private lateinit var btnPrev: ImageButton
    private lateinit var btnAdd: ImageButton
    private lateinit var txtCredit: TextView
    private lateinit var txtName: TextView
    private lateinit var txtPrice: TextView
    private lateinit var rtgBar: RatingBar

    companion object {
        const val ITEM = "ITEM"
        const val ITEM_INDEX = "ITEM_INDEX"
        const val ITEM_STATE = "ITEM_STATE"
        const val REMAIN_CREDIT_VALUE = "REMAIN_CREDIT_VALUE"
        const val CREDIT_VALUE = "CREDIT_VALUE"
        const val SELECTED_COLOR = "SELECTED_COLOR"
        const val CHECKOUT_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize components
        imgItem = findViewById(R.id.img_item)
        btnNext = findViewById(R.id.btn_next)
        btnPrev = findViewById(R.id.btn_prev)
        btnAdd = findViewById(R.id.btn_add)
        txtCredit = findViewById(R.id.txt_credit)
        txtCredit.text = "$creditValue"
        txtName = findViewById(R.id.txt_name)
        txtPrice = findViewById(R.id.txt_price)
        rtgBar = findViewById(R.id.rtg_bar)
        rtgBar.setIsIndicator(true)
        rgrColor = findViewById(R.id.rgr_color)
        val btnBorrow = findViewById<Button>(R.id.btn_borrow)

        // Handle "Add" button click
        btnAdd.setOnClickListener {
            creditValue += 20
            txtCredit.text = "$creditValue"
        }

        // Initialize default browse button state
        updateButtonState(btnPrev, btnNext)

        // Handle "Previous" button click
        btnPrev.setOnClickListener {
            if (itemIndex > 0) {
                itemIndex-- // Move to the previous item
                Log.d("Item Check","Item Name: ${listItem[itemIndex].name}")
                Log.d("Item Check","Item Borrowed: ${listItem[itemIndex].isBorrowed}")
                updateItemInfo(txtName, rtgBar, txtPrice, rgrColor, imgItem)
            }
            updateButtonState(btnPrev, btnNext)
        }

        // Handle "Next" button click
        btnNext.setOnClickListener {
            if (itemIndex < listItem.size - 1) {
                itemIndex++ // Move to the next item
                Log.d("Item Check","Item Name: ${listItem[itemIndex].name}")
                Log.d("Item Check","Item Borrowed: ${listItem[itemIndex].isBorrowed}")
                updateItemInfo(txtName, rtgBar, txtPrice, rgrColor, imgItem)
            }
            updateButtonState(btnPrev, btnNext)
        }

        // Handle color selection
        rgrColor.setOnCheckedChangeListener { _, checkedId ->
            val selectedColor = when (checkedId) {
                R.id.rbt_red -> getString(R.string.rbt_red)
                R.id.rbt_pink -> getString(R.string.rbt_pink)
                R.id.rbt_blue -> getString(R.string.rbt_blue)
                else -> ""
            }
            // Update the item's color
            listItem[itemIndex].attribute = selectedColor
        }

        // Handle "Borrow" button click -> change activity
        btnBorrow.setOnClickListener {
            if (!listItem[itemIndex].isBorrowed && (creditValue > listItem[itemIndex].price)) {
                val intentBorrow = Intent(this, CheckoutActivity::class.java).apply {
                    putExtra(ITEM_INDEX, itemIndex)     // Pass current Item object Index
                    putExtra(ITEM, listItem[itemIndex]) // Pass current Item object
                    putExtra(CREDIT_VALUE, creditValue) // Pass the current credit value
                }
                startActivityForResult(intentBorrow, CHECKOUT_CODE)
            } else {
                Toast.makeText(applicationContext, "Cannot Borrow!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // This method is called when the CheckoutActivity finishes
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHECKOUT_CODE && resultCode == RESULT_OK) {
            data?.let {
                val remainingCredit = it.getIntExtra(REMAIN_CREDIT_VALUE, creditValue)
                creditValue = remainingCredit
                Log.d("Credit Debug", "Remaining credit Value: $creditValue")
                findViewById<TextView>(R.id.txt_credit).text = "$creditValue"
                itemIndex = it.getIntExtra(ITEM_INDEX, 0)
                listItem[itemIndex].isBorrowed = it.getBooleanExtra("ITEM_STATE", false)
                Log.d("Passing Test", "Index Item: ${itemIndex}, Item Borrowed: ${listItem[itemIndex].isBorrowed}")
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ITEM_INDEX, itemIndex)
        outState.putInt(CREDIT_VALUE, creditValue)
        val selectedColorId = rgrColor.checkedRadioButtonId
        outState.putInt(SELECTED_COLOR, selectedColorId)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        itemIndex = savedInstanceState.getInt(ITEM_INDEX, 0)
        creditValue = savedInstanceState.getInt(CREDIT_VALUE, 299)
        val selectedColorId = savedInstanceState.getInt(SELECTED_COLOR)
        txtCredit.text = "$creditValue"
        updateItemInfo(txtName, rtgBar, txtPrice, rgrColor, imgItem)
        rgrColor.check(selectedColorId)
        updateButtonState(btnPrev, btnNext)
    }

    // Update Item Information when "Next" or "Previous" button clicked
    private fun updateItemInfo(name: TextView, rating: RatingBar, price: TextView, attribute: RadioGroup, imageView: ImageView) {
        // Update the image
        imageView.setImageResource(listItem[itemIndex].imageResId)
        // Update the name
        name.text = listItem[itemIndex].name
        // Update the rating
        rating.rating = listItem[itemIndex].rating
        // Update the price
        price.text = "${listItem[itemIndex].price}"
        //Reset Radio Group choice
        attribute.clearCheck()
        attribute.check(R.id.rbt_red)
    }

    // Update "Next" and "Previous" button state (clickable/non-clickable)
    private fun updateButtonState(button1: ImageButton, button2: ImageButton) {
        if (itemIndex > 0) {
            button1.isEnabled
            button1.setAlpha(1f)
        } else {
            button1.setAlpha(0.5f)
        }
        if (itemIndex < listItem.size - 1) {
            button2.isEnabled
            button2.setAlpha(1f)
        } else {
            button2.setAlpha(0.5f)
        }
    }
}