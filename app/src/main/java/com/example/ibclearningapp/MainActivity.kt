
package com.example.ibclearningapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Dish(
    val id: String,
    var name: String,
    var price: String,
    var category: String,
    var ingredients: String = "",
    var gsa: String = "",
    var allergens: String = "",
    var addons: String = "",
    var image: String = ""
)

class MainActivity : Activity() {

    private val bgColor = Color.rgb(7, 19, 15)
    private val forest = Color.rgb(10, 31, 24)
    private val panel = Color.rgb(17, 42, 32)
    private val panelLight = Color.rgb(24, 54, 41)
    private val gold = Color.rgb(218, 181, 108)
    private val cream = Color.rgb(246, 241, 229)
    private val muted = Color.rgb(164, 177, 166)
    private val line = Color.rgb(48, 75, 59)

    private val IMAGE_REQUEST = 1001
    private val EXPORT_REQUEST = 1002
    private val IMPORT_REQUEST = 1003

    private var pendingImageDish: Dish? = null

    private val categories = mutableListOf(
        "Salads", "Soups", "Starters", "Dynamites",
        "Baked Potatoes", "Pasta & Special", "Burgers",
        "Pizzas", "Desserts", "Fresh Juices", "Mojitos",
        "Milkshakes", "Smoothies", "Hot Coffees",
        "Cold Coffee", "Other Drinks", "L.T.O."
    )

    private val dishes = mutableListOf<Dish>()
    private var selectedCategory = "All"
    private var searchText = ""

    private lateinit var dishContainer: LinearLayout
    private lateinit var searchInput: EditText

    // Keep the same storage key so existing saved menu data is retained.
    private val prefs by lazy {
        getSharedPreferences("IBC_MENU_DATA", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadData()
        showSplash()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun rounded(
        color: Int,
        radius: Int = 18,
        strokeColor: Int = Color.TRANSPARENT
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (strokeColor != Color.TRANSPARENT) {
                setStroke(dp(1), strokeColor)
            }
        }
    }

    private fun text(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false,
        align: Int = Gravity.START
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            gravity = align
            if (bold) typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(3), dp(4), dp(3), dp(4))
        }
    }

    private fun actionButton(
        value: String,
        filled: Boolean = true,
        compact: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = if (compact) 12f else 13f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(if (filled) bgColor else gold)
            setPadding(
                dp(if (compact) 13 else 16),
                dp(if (compact) 10 else 14),
                dp(if (compact) 13 else 16),
                dp(if (compact) 10 else 14)
            )
            background = rounded(
                if (filled) gold else panelLight,
                13,
                if (filled) gold else line
            )
            isClickable = true
            isFocusable = true
        }
    }

    private fun loadData() {
        try {
            val saved = prefs.getString("menu", null) ?: return
            val root = JSONObject(saved)

            val savedCategories = root.optJSONArray("categories")
            if (savedCategories != null) {
                categories.clear()
                for (i in 0 until savedCategories.length()) {
                    categories.add(savedCategories.getString(i))
                }
            }

            dishes.clear()
            val savedDishes = root.optJSONArray("dishes")

            if (savedDishes != null) {
                for (i in 0 until savedDishes.length()) {
                    val item = savedDishes.getJSONObject(i)
                    dishes.add(
                        Dish(
                            id = item.optString(
                                "id",
                                UUID.randomUUID().toString()
                            ),
                            name = item.optString("name"),
                            price = item.optString("price"),
                            category = item.optString("category"),
                            ingredients = item.optString("ingredients"),
                            gsa = item.optString("gsa"),
                            allergens = item.optString("allergens"),
                            addons = item.optString("addons"),
                            image = item.optString("image")
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Could not load menu: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun saveData() {
        try {
            val root = JSONObject()
            val categoryArray = JSONArray()
            val dishArray = JSONArray()

            categories.forEach { categoryArray.put(it) }

            dishes.forEach { dish ->
                val item = JSONObject()
                item.put("id", dish.id)
                item.put("name", dish.name)
                item.put("price", dish.price)
                item.put("category", dish.category)
                item.put("ingredients", dish.ingredients)
                item.put("gsa", dish.gsa)
                item.put("allergens", dish.allergens)
                item.put("addons", dish.addons)
                item.put("image", dish.image)
                dishArray.put(item)
            }

            root.put("categories", categoryArray)
            root.put("dishes", dishArray)

            prefs.edit().putString("menu", root.toString()).apply()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Could not save menu: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showSplash() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(28), dp(28), dp(28), dp(28))
            background = rounded(bgColor, 0)
        }

        root.addView(text("✦", 42f, gold, true, Gravity.CENTER))
        root.addView(text("I B C", 43f, cream, true, Gravity.CENTER))
        root.addView(
            text("MENU MANAGEMENT", 13f, gold, true, Gravity.CENTER)
        )
        root.addView(
            text("BY ANMOL RAI", 11f, muted, false, Gravity.CENTER)
        )

        setContentView(root)

        android.os.Handler(mainLooper).postDelayed({
            showHomeScreen()
        }, 2500)
    }

    private fun categoryButton(
        label: String,
        selected: Boolean
    ): TextView {
        return TextView(this).apply {
            text = label
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(if (selected) bgColor else cream)
            setPadding(dp(15), 0, dp(15), 0)
            background = rounded(
                if (selected) gold else panel,
                13,
                if (selected) gold else line
            )
        }
    }

    private fun showHomeScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bgColor)
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(20), dp(18), dp(25))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = rounded(forest, 20, line)
        }

        val brandRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        brand.addView(text("✦  IBC APP", 27f, gold, true))
        brand.addView(
            text("MENU MANAGEMENT  •  BY ANMOL RAI", 10f, muted, true)
        )

        brandRow.addView(
            brand,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        val brandMark = TextView(this).apply {
            text = "IBC"
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(gold)
            background = rounded(panelLight, 15, gold)
            setPadding(dp(13), dp(13), dp(13), dp(13))
        }

        brandRow.addView(brandMark)
        header.addView(brandRow)
        header.addView(text("WELCOME BACK", 10f, gold, true))
        header.addView(
            text("Your menu, beautifully managed.", 15f, cream)
        )
        content.addView(header)

        content.addView(
            text("Find a dish", 17f, cream, true).apply {
                setPadding(0, dp(20), 0, dp(7))
            }
        )

        searchInput = EditText(this).apply {
            hint = "⌕   Search your menu..."
            textSize = 14f
            setSingleLine(true)
            setTextColor(cream)
            setHintTextColor(muted)
            setPadding(dp(16), dp(12), dp(16), dp(12))
            background = rounded(panel, 14, line)
        }

        content.addView(
            searchInput,
            LinearLayout.LayoutParams(-1, dp(52))
        )

        content.addView(
            text("MENU SECTIONS", 17f, cream, true).apply {
                setPadding(0, dp(20), 0, dp(8))
            }
        )

        val categoryScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            clipToPadding = false
        }

        val categoryRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val allButton = categoryButton(
            "All",
            selectedCategory == "All"
        )
        allButton.setOnClickListener {
            selectedCategory = "All"
            showHomeScreen()
        }
        categoryRow.addView(allButton)

        categories.forEach { category ->
            val categoryView = categoryButton(
                category,
                selectedCategory == category
            )
            categoryView.setOnClickListener {
                selectedCategory = category
                showHomeScreen()
            }
            categoryRow.addView(
                categoryView,
                LinearLayout.LayoutParams(-2, dp(42)).apply {
                    leftMargin = dp(8)
                }
            )
        }

        categoryScroll.addView(categoryRow)
        content.addView(
            categoryScroll,
            LinearLayout.LayoutParams(-1, dp(48))
        )

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val addDish = actionButton("+  ADD DISH", true)
        addDish.setOnClickListener {
            showDishEditor(null)
        }

        val manageSections = actionButton("MANAGE SECTIONS", false)
        manageSections.setOnClickListener {
            manageCategories()
        }

        actions.addView(
            addDish,
            LinearLayout.LayoutParams(0, dp(49), 1f)
        )
        actions.addView(
            manageSections,
            LinearLayout.LayoutParams(0, dp(49), 1f).apply {
                leftMargin = dp(9)
            }
        )

        content.addView(
            actions,
            LinearLayout.LayoutParams(-1, dp(49)).apply {
                topMargin = dp(17)
                bottomMargin = dp(20)
            }
        )

        val heading = if (selectedCategory == "All") {
            "ALL DISHES"
        } else {
            selectedCategory.uppercase()
        }

        content.addView(text(heading, 17f, gold, true))

        content.addView(
            text(
                "${dishes.count {
                    selectedCategory == "All" ||
                    it.category == selectedCategory
                }} items",
                11f,
                muted
            )
        )

        dishContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        content.addView(
            dishContainer,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(12)
            }
        )

        val moreOptions = actionButton("•••   MORE OPTIONS", false)
        moreOptions.setOnClickListener {
            showBackupInfo()
        }

        content.addView(
            moreOptions,
            LinearLayout.LayoutParams(-1, dp(49)).apply {
                topMargin = dp(22)
            }
        )

        content.addView(
            text(
                "IBC APP  •  CRAFTED FOR BETTER SERVICE",
                9f,
                muted,
                true,
                Gravity.CENTER
            ),
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(20)
            }
        )

        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, -1))
        setContentView(root)

        searchInput.setText(searchText)
        searchInput.setSelection(searchInput.text.length)

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                searchText = s.toString()
                renderDishes()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        renderDishes()
    }

    private fun renderDishes() {
        if (!::dishContainer.isInitialized) return

        dishContainer.removeAllViews()

        val filtered = dishes.filter { dish ->
            val matchesCategory =
                selectedCategory == "All" ||
                dish.category == selectedCategory

            val matchesSearch =
                dish.name.contains(searchText, ignoreCase = true) ||
                dish.category.contains(searchText, ignoreCase = true)

            matchesCategory && matchesSearch
        }

        if (filtered.isEmpty()) {
            val empty = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(20), dp(30), dp(20), dp(30))
                background = rounded(panel, 18, line)
            }

            empty.addView(text("✦", 28f, gold, true, Gravity.CENTER))
            empty.addView(
                text("No dishes found", 16f, cream, true, Gravity.CENTER)
            )
            empty.addView(
                text(
                    "Add a dish to start building your menu.",
                    12f,
                    muted,
                    false,
                    Gravity.CENTER
                )
            )
            dishContainer.addView(empty)
            return
        }

        filtered.forEach { dish ->
            dishContainer.addView(dishCard(dish))
        }
        
    private fun dishCard(dish: Dish): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(15), dp(12), dp(12), dp(12))
            background = rounded(panel, 16, line)
        }

        val nameView = TextView(this).apply {
            text = dish.name
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(cream)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(3), dp(8), dp(8), dp(8))
            isClickable = true
            isFocusable = true
            setOnClickListener {
                showDishDetails(dish)
            }
        }

        card.addView(
            nameView,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        val options = actionButton("•••", false, true)
        options.setOnClickListener {
            showDishOptions(dish)
        }

        card.addView(
            options,
            LinearLayout.LayoutParams(-2, dp(42))
        )

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(card, LinearLayout.LayoutParams(-1, -2))
            setPadding(0, 0, 0, dp(9))
        }
    }

    private fun infoLine(
        parent: LinearLayout,
        label: String,
        value: String
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(5), 0, dp(3))
        }

        row.addView(text(label, 10f, gold, true))
        row.addView(text(value, 13f, cream))
        parent.addView(row)
    }

    private fun showDishDetails(dish: Dish) {
        val details = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(10), dp(18), dp(14))
        }

        if (dish.image.isNotBlank()) {
            try {
                val imageView = ImageView(this).apply {
                    setImageURI(Uri.parse(dish.image))
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    background = rounded(panelLight, 14)
                    clipToOutline = true
                }

                details.addView(
                    imageView,
                    LinearLayout.LayoutParams(-1, dp(205)).apply {
                        bottomMargin = dp(12)
                    }
                )
            } catch (_: Exception) {
                details.addView(
                    text("Picture could not be loaded", 12f, muted)
                )
            }
        }

        details.addView(text(dish.name, 23f, cream, true))
        details.addView(text("OMR ${dish.price}", 18f, gold, true))
        details.addView(
            text("SECTION  •  ${dish.category}", 11f, muted, true)
        )

        infoLine(
            details,
            "INGREDIENTS",
            dish.ingredients.ifBlank { "Not added yet" }
        )
        infoLine(
            details,
            "GSA – GOLD STANDARD ATTRIBUTE",
            dish.gsa.ifBlank { "Not added yet" }
        )
        infoLine(
            details,
            "ALLERGENS",
            dish.allergens.ifBlank { "Not added yet" }
        )
        infoLine(
            details,
            "ADD-ONS",
            dish.addons.ifBlank { "Not added yet" }
        )

        val optionsButton = actionButton("•••   DISH OPTIONS", false)
        val scroll = ScrollView(this).apply {
            addView(details)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Dish Details")
            .setView(scroll)
            .setPositiveButton("Close", null)
            .create()

        optionsButton.setOnClickListener {
            dialog.dismiss()
            showDishOptions(dish)
        }

        details.addView(
            optionsButton,
            LinearLayout.LayoutParams(-1, dp(46)).apply {
                topMargin = dp(14)
            }
        )

        dialog.show()
    }

    private fun showDishOptions(dish: Dish) {
        val options = arrayOf(
            "Ingredients",
            "GSA – Gold Standard Attribute",
            "Allergens",
            "Manage Add-ons",
            "Edit Dish",
            "Edit Price",
            "Change Food Picture",
            "Delete Dish"
        )

        AlertDialog.Builder(this)
            .setTitle(dish.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editInformation(dish, "Ingredients")
                    1 -> editInformation(dish, "GSA")
                    2 -> editInformation(dish, "Allergens")
                    3 -> manageAddons(dish)
                    4 -> showDishEditor(dish)
                    5 -> editPrice(dish)
                    6 -> chooseFoodPicture(dish)
                    7 -> deleteDish(dish)
                }
            }
            .show()
    }

    private fun showDishEditor(existing: Dish?) {
        val nameInput = EditText(this).apply {
            hint = "Dish name"
            setText(existing?.name ?: "")
            setTextColor(cream)
            setHintTextColor(muted)
        }

        val priceInput = EditText(this).apply {
            hint = "Price in OMR"
            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(existing?.price ?: "")
            setTextColor(cream)
            setHintTextColor(muted)
        }

        val categorySpinner = Spinner(this)
        val categoryOptions = categories.toMutableList()

        if (categoryOptions.isEmpty()) {
            categoryOptions.add("Uncategorized")
        }

        categorySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categoryOptions
        )

        val currentIndex = categoryOptions.indexOf(existing?.category)
        if (currentIndex >= 0) {
            categorySpinner.setSelection(currentIndex)
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), dp(8))
            addView(text("DISH NAME", 10f, gold, true))
            addView(nameInput)
            addView(text("PRICE (OMR)", 10f, gold, true))
            addView(priceInput)
            addView(text("MENU SECTION", 10f, gold, true))
            addView(categorySpinner)
        }

        val scroll = ScrollView(this).apply {
            addView(layout)
        }

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Add New Dish" else "Edit Dish")
            .setView(scroll)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val name = nameInput.text.toString().trim()
                val price = priceInput.text.toString().trim()
                val category = categorySpinner.selectedItem.toString()

                if (name.isEmpty() || price.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Enter both dish name and price",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                if (existing == null) {
                    dishes.add(
                        Dish(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            price = price,
                            category = category
                        )
                    )
                } else {
                    existing.name = name
                    existing.price = price
                    existing.category = category
                }

                saveData()
                showHomeScreen()
            }
            .show()
    }

    private fun editInformation(dish: Dish, type: String) {
        val currentValue = when (type) {
            "Ingredients" -> dish.ingredients
            "GSA" -> dish.gsa
            else -> dish.allergens
        }

        val input = EditText(this).apply {
            hint = "Enter $type"
            setText(currentValue)
            setTextColor(cream)
            setHintTextColor(muted)
            minLines = 3
            gravity = Gravity.TOP
        }

        val scroll = ScrollView(this).apply {
            setPadding(dp(18), dp(8), dp(18), dp(8))
            addView(input)
        }

        AlertDialog.Builder(this)
            .setTitle("$type – ${dish.name}")
            .setView(scroll)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val value = input.text.toString().trim()

                when (type) {
                    "Ingredients" -> dish.ingredients = value
                    "GSA" -> dish.gsa = value
                    "Allergens" -> dish.allergens = value
                }

                saveData()
                showHomeScreen()
            }
            .show()
    }

    private fun manageAddons(dish: Dish) {
        val input = EditText(this).apply {
            hint = "Example:\nExtra cheese - 0.500\nExtra sauce - 0.300"
            setText(dish.addons)
            setTextColor(cream)
            setHintTextColor(muted)
            minLines = 4
            gravity = Gravity.TOP
        }

        val scroll = ScrollView(this).apply {
            setPadding(dp(18), dp(8), dp(18), dp(8))
            addView(input)
        }

        AlertDialog.Builder(this)
            .setTitle("Manage Add-ons")
            .setMessage("Enter each add-on on a separate line.")
            .setView(scroll)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                dish.addons = input.text.toString().trim()
                saveData()
                showHomeScreen()
            }
            .show()
    }

    private fun editPrice(dish: Dish) {
        val input = EditText(this).apply {
            hint = "Price in OMR"
            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(dish.price)
            setTextColor(cream)
            setHintTextColor(muted)
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Price")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val price = input.text.toString().trim()

                if (price.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Enter a valid price",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    dish.price = price
                    saveData()
                    showHomeScreen()
                }
            }
            .show()
    }

    private fun chooseFoodPicture(dish: Dish) {
        pendingImageDish = dish

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }

        startActivityForResult(intent, IMAGE_REQUEST)
    }

    private fun deleteDish(dish: Dish) {
        AlertDialog.Builder(this)
            .setTitle("Delete Dish")
            .setMessage("Are you sure you want to delete ${dish.name}?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                dishes.remove(dish)
                saveData()
                showHomeScreen()

                Toast.makeText(
                    this,
                    "Dish deleted",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }

    private fun manageCategories() {
        val options = arrayOf(
            "Add Section",
            "Rename Section",
            "Delete Section"
        )

        AlertDialog.Builder(this)
            .setTitle("Manage Menu Sections")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> addCategory()
                    1 -> renameCategory()
                    2 -> deleteCategory()
                }
            }
            .show()
    }

    private fun addCategory() {
        val input = EditText(this).apply {
            hint = "Enter new section name"
            setTextColor(cream)
            setHintTextColor(muted)
        }

        AlertDialog.Builder(this)
            .setTitle("Add Section")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add") { _, _ ->
                val name = input.text.toString().trim()

                if (name.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Enter section name",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (categories.any { it.equals(name, true) }) {
                    Toast.makeText(
                        this,
                        "Section already exists",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    categories.add(name)
                    saveData()
                    showHomeScreen()
                }
            }
            .show()
    }

    private fun renameCategory() {
        if (categories.isEmpty()) {
            Toast.makeText(
                this,
                "No sections available",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        val input = EditText(this).apply {
            hint = "New section name"
            setTextColor(cream)
            setHintTextColor(muted)
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(10), dp(20), dp(10))
            addView(text("SELECT SECTION", 12f, gold, true))
            addView(spinner)
            addView(input)
        }

        AlertDialog.Builder(this)
            .setTitle("Rename Section")
            .setView(layout)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Rename") { _, _ ->
                val oldName = spinner.selectedItem.toString()
                val newName = input.text.toString().trim()

                if (
                    newName.isNotEmpty() &&
                    !categories.any {
                        it.equals(newName, true) && it != oldName
                    }
                ) {
                    val index = categories.indexOf(oldName)
                    if (index >= 0) categories[index] = newName

                    dishes.filter {
                        it.category == oldName
                    }.forEach {
                        it.category = newName
                    }

                    if (selectedCategory == oldName) {
                        selectedCategory = newName
                    }

                    saveData()
                    showHomeScreen()
                } else {
                    Toast.makeText(
                        this,
                        "Enter a valid, unique section name",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .show()
    }

    private fun deleteCategory() {
        if (categories.isEmpty()) {
            Toast.makeText(
                this,
                "No sections available",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        AlertDialog.Builder(this)
            .setTitle("Delete Section")
            .setMessage(
                "Dishes in this section will remain saved but become uncategorized."
            )
            .setView(spinner)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                val category = spinner.selectedItem.toString()

                dishes.filter {
                    it.category == category
                }.forEach {
                    it.category = "Uncategorized"
                }

                categories.remove(category)

                if (selectedCategory == category) {
                    selectedCategory = "All"
                }

                saveData()
                showHomeScreen()
            }
            .show()
    }

    private fun showBackupInfo() {
        val options = arrayOf(
            "Backup Menu (Export)",
            "Restore Menu (Import)"
        )

        AlertDialog.Builder(this)
            .setTitle("More Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> exportBackup()
                    1 -> importBackup()
                }
            }
            .show()
    }

    private fun createBackup(): String {
        val root = JSONObject()
        val categoryArray = JSONArray()
        val dishArray = JSONArray()

        categories.forEach {
            categoryArray.put(it)
        }

        dishes.forEach { dish ->
            val item = JSONObject()
            item.put("id", dish.id)
            item.put("name", dish.name)
            item.put("price", dish.price)
            item.put("category", dish.category)
            item.put("ingredients", dish.ingredients)
            item.put("gsa", dish.gsa)
            item.put("allergens", dish.allergens)
            item.put("addons", dish.addons)
            item.put("image", dish.image)
            dishArray.put(item)
        }

        root.put("categories", categoryArray)
        root.put("dishes", dishArray)

        return root.toString(2)
    }

    private fun exportBackup() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(
                Intent.EXTRA_TITLE,
                "IBC_Menu_Backup.json"
            )
        }

        startActivityForResult(intent, EXPORT_REQUEST)
    }

    private fun importBackup() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf("application/json", "text/plain")
            )
        }

        startActivityForResult(intent, IMPORT_REQUEST)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK) return

        val uri = data?.data ?: return

        when (requestCode) {
            IMAGE_REQUEST -> {
                try {
                    val flags =
                        data.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION

                    try {
                        contentResolver.takePersistableUriPermission(
                            uri,
                            flags
                        )
                    } catch (_: Exception) {
                      
    }
    
