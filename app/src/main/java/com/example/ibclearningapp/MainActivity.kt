
package com.example.ibclearningapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject

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

    private val forest = Color.rgb(15, 48, 36)
    private val green = Color.rgb(30, 78, 57)
    private val gold = Color.rgb(218, 180, 105)
    private val cream = Color.rgb(250, 247, 239)
    private val ink = Color.rgb(40, 43, 37)
    private val muted = Color.rgb(115, 115, 105)

    private val categories = mutableListOf(
        "Salads",
        "Soups",
        "Starters",
        "Dynamites",
        "Baked Potatoes",
        "Pasta & Special",
        "Burgers",
        "Pizzas",
        "Desserts",
        "Fresh Juices",
        "Mojitos",
        "Milkshakes",
        "Smoothies",
        "Hot Coffees",
        "Cold Coffee",
        "Other Drinks",
        "L.T.O."
    )

    private val dishes = mutableListOf<Dish>()

    private var selectedCategory = "All"
    private var searchText = ""

    private lateinit var dishContainer: LinearLayout
    private lateinit var searchInput: EditText

    private val prefs by lazy {
        getSharedPreferences("IBC_MENU_DATA", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadData()
        showSplash()
    }

    private fun loadData() {
        try {
            val saved = prefs.getString("menu", null)
            if (saved != null) {
                val root = JSONObject(saved)

                categories.clear()
                val savedCategories = root.getJSONArray("categories")

                for (i in 0 until savedCategories.length()) {
                    categories.add(savedCategories.getString(i))
                }

                dishes.clear()
                val savedDishes = root.getJSONArray("dishes")

                for (i in 0 until savedDishes.length()) {
                    val item = savedDishes.getJSONObject(i)

                    dishes.add(
                        Dish(
                            id = item.optString("id"),
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
        } catch (_: Exception) {
            dishes.clear()
        }
    }

    private fun saveData() {
        try {
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

            prefs.edit().putString("menu", root.toString()).apply()

        } catch (_: Exception) {
            Toast.makeText(this, "Could not save menu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSplash() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(forest)
            setPadding(24, 24, 24, 24)
        }

        root.addView(text("✦", 38, gold, true))
        root.addView(text("I B C", 42, cream, true))
        root.addView(text("MENU MANAGEMENT", 14, gold, true))
        root.addView(text("BY ANMOL RAI", 12, cream, false))

        setContentView(root)

        android.os.Handler(mainLooper).postDelayed({
            showHomeScreen()
        }, 3000)
    }

    private fun text(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER
            if (bold) {
                typeface = Typeface.DEFAULT_BOLD
            }
            setPadding(8, 8, 8, 8)
        }
    }

    private fun button(
        value: String,
        backgroundColor: Int = forest
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = 14f
            setTextColor(cream)
            gravity = Gravity.CENTER
            setPadding(18, 14, 18, 14)
            setBackgroundColor(backgroundColor)
            isClickable = true
            isFocusable = true
        }
    }

    private fun showHomeScreen() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(cream)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 22, 18, 20)
            setBackgroundColor(forest)
        }

        header.addView(
            text("✦  IBC APP", 25, gold, true)
        )

        header.addView(
            text("MENU MANAGEMENT  •  BY ANMOL RAI", 11, cream)
        )

        root.addView(header)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 14, 16, 16)
        }

        content.addView(
            text("Your Menu", 25, forest, true)
        )

        content.addView(
            text("Manage your dishes and menu details", 13, muted)
        )

        searchInput = EditText(this).apply {
            hint = "Search dishes..."
            textSize = 15f
            singleLine = true
            setTextColor(ink)
            setHintTextColor(muted)
            setPadding(14, 12, 14, 12)
            setBackgroundColor(Color.WHITE)
        }

        content.addView(
            searchInput,
            LinearLayout.LayoutParams(
                -1,
                52
            ).apply {
                topMargin = 14
                bottomMargin = 14
            }
        )

        val stats = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        stats.addView(
            statCard("CATEGORIES", categories.size.toString()),
            LinearLayout.LayoutParams(0, 75, 1f)
        )

        stats.addView(
            statCard("TOTAL DISHES", dishes.size.toString()),
            LinearLayout.LayoutParams(0, 75, 1f)
        )

        content.addView(stats)

        content.addView(
            text("MENU SECTIONS", 13, forest, true)
        )

        val categoryScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
        }

        val categoryRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val allButton = button("All", green)
        allButton.setOnClickListener {
            selectedCategory = "All"
            showHomeScreen()
        }
        categoryRow.addView(allButton)

        categories.forEach { category ->
            val categoryButton = button(category, forest)

            categoryButton.setOnClickListener {
                selectedCategory = category
                showHomeScreen()
            }

            categoryRow.addView(
                categoryButton,
                LinearLayout.LayoutParams(
                    -2,
                    -2
                ).apply {
                    leftMargin = 7
                }
            )
        }

        categoryScroll.addView(categoryRow)

        content.addView(
            categoryScroll,
            LinearLayout.LayoutParams(-1, 55)
        )

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val addDishButton = button("+ Add Dish", green)
        addDishButton.setOnClickListener {
            showDishEditor(null)
        }

        val manageButton = button("Manage Sections", forest)
        manageButton.setOnClickListener {
            manageCategories()
        }

        actions.addView(
            addDishButton,
            LinearLayout.LayoutParams(0, 52, 1f)
        )

        actions.addView(
            manageButton,
            LinearLayout.LayoutParams(0, 52, 1f).apply {
                leftMargin = 8
            }
        )

        content.addView(
            actions,
            LinearLayout.LayoutParams(-1, 52).apply {
                topMargin = 12
                bottomMargin = 12
            }
        )

        content.addView(
            text(
                if (selectedCategory == "All") "ALL DISHES"
                else selectedCategory.uppercase(),
                15,
                forest,
                true
            )
        )

        val dishScroll = ScrollView(this)

        dishContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        dishScroll.addView(dishContainer)

        content.addView(
            dishScroll,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        val backupButton = button("BACKUP & RESTORE", forest)
        backupButton.setOnClickListener {
            showBackupInfo()
        }

        content.addView(
            backupButton,
            LinearLayout.LayoutParams(-1, 50).apply {
                topMargin = 10
            }
        )

        root.addView(
            content,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        setContentView(root)

        searchInput.setText(searchText)

        searchInput.addTextChangedListener(
            object : TextWatcher {
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
            }
        )

        renderDishes()
    }

    private fun statCard(
        label: String,
        value: String
    ): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            setPadding(8, 8, 8, 8)
        }

        card.addView(text(value, 22, forest, true))
        card.addView(text(label, 10, muted, true))

        return card
    }

    private fun dishCard(dish: Dish): LinearLayout {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 12, 14, 12)
            setBackgroundColor(Color.WHITE)
        }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val details = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        details.addView(text(dish.name, 17, forest, true))
        details.addView(text(dish.category, 12, muted))
        details.addView(text("OMR ${dish.price}", 15, green, true))

        row.addView(
            details,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        val more = button("⋮", forest)
        more.setOnClickListener {
            showDishOptions(dish)
        }

        row.addView(more)

        card.addView(row)

        if (dish.ingredients.isNotBlank()) {
            card.addView(
                text("Ingredients: ${dish.ingredients}", 12, muted)
            )
        }

        if (dish.gsa.isNotBlank()) {
            card.addView(
                text("GSA: ${dish.gsa}", 12, muted)
            )
        }

        if (dish.allergens.isNotBlank()) {
            card.addView(
                text("Allergens: ${dish.allergens}", 12, muted)
            )
        }

        card.setOnClickListener {
            showDishOptions(dish)
        }

        val params = LinearLayout.LayoutParams(-1, -2)
        params.bottomMargin = 10
        card.layoutParams = params

        return card
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

        val fields = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 10, 20, 5)
        }

        val nameInput = EditText(this).apply {
            hint = "Dish name"
            setText(existing?.name ?: "")
        }

        val priceInput = EditText(this).apply {
            hint = "Price in OMR (example: 1.500)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(existing?.price ?: "")
        }

        val categorySpinner = Spinner(this)
        val categoryOptions = categories.toMutableList()

        if (existing != null && !categoryOptions.contains(existing.category)) {
            categoryOptions.add(existing.category)
        }

        categorySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categoryOptions
        )

        if (existing != null) {
            val position = categoryOptions.indexOf(existing.category)
            if (position >= 0) categorySpinner.setSelection(position)
        }

        fields.addView(nameInput)
        fields.addView(priceInput)
        fields.addView(text("Select menu section", 13, forest, true))
        fields.addView(categorySpinner)

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Add Dish" else "Edit Dish")
            .setView(fields)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {

                    val name = nameInput.text.toString().trim()
                    val price = priceInput.text.toString().trim()

                    if (name.isEmpty() || price.isEmpty()) {
                        Toast.makeText(
                            this,
                            "Enter dish name and price",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    if (existing == null) {
                        dishes.add(
                            Dish(
                                id = java.util.UUID.randomUUID().toString(),
                                name = name,
                                price = price,
                                category = categorySpinner.selectedItem.toString()
                            )
                        )
                    } else {
                        existing.name = name
                        existing.price = price
                        existing.category = categorySpinner.selectedItem.toString()
                    }

                    saveData()
                    dialog.dismiss()
                    showHomeScreen()
                }
        }

        dialog.show()
    }

    private fun editInformation(dish: Dish, type: String) {

        val current = when (type) {
            "Ingredients" -> dish.ingredients
            "GSA" -> dish.gsa
            else -> dish.allergens
        }

        val input = EditText(this).apply {
            hint = "Enter $type"
            setText(current)
            minLines = 3
            gravity = Gravity.TOP
        }

        AlertDialog.Builder(this)
            .setTitle(type)
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->

                when (type) {
                    "Ingredients" -> dish.ingredients = input.text.toString()
                    "GSA" -> dish.gsa = input.text.toString()
                    "Allergens" -> dish.allergens = input.text.toString()
                }

                saveData()
                showHomeScreen()
            }
            .show()
    }

    private fun manageAddons(dish: Dish) {

        val input = EditText(this).apply {
            hint = "One add-on per line:\nExtra cheese, 0.500\nExtra sauce, 0.300"
            setText(dish.addons)
            minLines = 4
            gravity = Gravity.TOP
        }

        AlertDialog.Builder(this)
            .setTitle("Manage Add-ons")
            .setMessage("Enter add-on name and price, one per line.")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                dish.addons = input.text.toString()
                saveData()
                showHomeScreen()
            }
            .show()
    }

    private fun editPrice(dish: Dish) {

        val input = EditText(this).apply {
            hint = "Price in OMR"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(dish.price)
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Price")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->

                val price = input.text.toString().trim()

                if (price.isNotEmpty()) {
                    dish.price = price
                    saveData()
                    showHomeScreen()
                }
            }
            .show()
    }

    private fun chooseFoodPicture(dish: Dish) {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }

        selectedImageDishId = dish.id

        startActivityForResult(intent, IMAGE_REQUEST)
    }

    private var selectedImageDishId: String? = null

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_REQUEST &&
            resultCode == RESULT_OK &&
            data?.data != null
        ) {
            val uri = data.data!!

            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}

            val dish = dishes.find { it.id == selectedImageDishId }

            if (dish != null) {
                dish.image = uri.toString()
                saveData()
                showHomeScreen()
            }
        }
    }

    private fun deleteDish(dish: Dish) {

        AlertDialog.Builder(this)
            .setTitle("Delete Dish")
            .setMessage("Delete ${dish.name}? This cannot be undone.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                dishes.remove(dish)
                saveData()
                showHomeScreen()
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
            hint = "New section name"
        }

        AlertDialog.Builder(this)
            .setTitle("Add Section")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add") { _, _ ->

                val name = input.text.toString().trim()

                if (name.isNotEmpty() && !categories.contains(name)) {
                    categories.add(name)
                    saveData()
                    showHomeScreen()
                }
            }
            .show()
    }

    private fun renameCategory() {

        if (categories.isEmpty()) return

        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        val input = EditText(this).apply {
            hint = "New section name"
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 5, 20, 5)
            addView(spinner)
            addView(input)
        }

        AlertDialog.Builder(this)
            .setTitle("Rename Section")
            .setView(layout)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->

                val oldName = spinner.selectedItem.toString()
                val newName = input.text.toString().trim()

                if (newName.isNotEmpty() &&
                    (newName == oldName || !categories.contains(newName))
                ) {
                    val index = categories.indexOf(oldName)
                    categories[index] = newName

                    dishes.filter { it.category == oldName }
                        .forEach { it.category = newName }

                    if (selectedCategory == oldName) {
                        selectedCategory = newName
                    }

                    saveData()
                    showHomeScreen()
                }
            }
            .show()
    }

    private fun deleteCategory() {

        if (categories.isEmpty()) return

        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        AlertDialog.Builder(this)
            .setTitle("Delete Section")
            .setMessage("Dishes in this section will also be deleted.")
            .setView(spinner)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->

                val category = spinner.selectedItem.toString()

                categories.remove(category)
                dishes.removeAll { it.category == category }

                if (selectedCategory == category) {
                    selectedCategory = "All"
                }

                saveData()
                showHomeScreen()
            }
            .show()
    }

    private fun showBackupInfo() {

        AlertDialog.Builder(this)
            .setTitle("IBC APP Backup")
            .setMessage(
                "Menu data is currently saved on this phone.\n\n" +
                "ZIP backup export, image backup and replace-mode restore " +
                "will be added in the next stage."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    companion object {
        private const val IMAGE_REQUEST = 501
    }
}

    private fun renderDishes() {

        dishContainer.removeAllViews()

        val filtered = dishes.filter { dish ->

            val categoryMatches =
                selectedCategory == "All" ||
                dish.category == selectedCategory

            val searchMatches =
                dish.name.contains(searchText, true) ||
                dish.category.contains(searchText, true)

            categoryMatches && searchMatches
        }

        if (filtered.isEmpty()) {
            dishContainer.addView(
                text(
                    "No dishes yet.\nTap + Add Dish to create your first menu item.",
                    15,
                    muted
                )
            )
            return
        }

        filtered.forEach { dish ->
            dishContainer.addView(dishCard(dish))
        }
    }
    
