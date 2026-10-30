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

    private val forest = Color.rgb(15, 48, 36)
    private val green = Color.rgb(30, 78, 57)
    private val gold = Color.rgb(218, 180, 105)
    private val cream = Color.rgb(250, 247, 239)
    private val ink = Color.rgb(40, 43, 37)
    private val muted = Color.rgb(115, 115, 105)

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
            val saved = prefs.getString("menu", null) ?: return
            val root = JSONObject(saved)

            categories.clear()
            val savedCategories = root.optJSONArray("categories")

            if (savedCategories != null) {
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
        } catch (_: Exception) {
            Toast.makeText(
                this,
                "Could not load menu",
                Toast.LENGTH_SHORT
            ).show()
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
            Toast.makeText(
                this,
                "Could not save menu",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showSplash() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(forest)
            setPadding(24, 24, 24, 24)
        }

        root.addView(text("✦", 38f, gold, true))
        root.addView(text("I B C", 42f, cream, true))
        root.addView(text("MENU MANAGEMENT", 14f, gold, true))
        root.addView(text("BY ANMOL RAI", 12f, cream))

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

        header.addView(text("✦  IBC APP", 25f, gold, true))
        header.addView(
            text("MENU MANAGEMENT • BY ANMOL RAI", 11f, cream)
        )

        root.addView(header)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 14, 16, 16)
        }

        content.addView(text("Your Menu", 25f, forest, true))
        content.addView(
            text("Manage your dishes and menu details", 13f, muted)
        )

        searchInput = EditText(this).apply {
            hint = "Search dishes..."
            textSize = 15f
            setSingleLine(true)
            setTextColor(ink)
            setHintTextColor(muted)
            setPadding(14, 12, 14, 12)
            setBackgroundColor(Color.WHITE)
        }

        content.addView(
            searchInput,
            LinearLayout.LayoutParams(-1, 52).apply {
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
        content.addView(text("MENU SECTIONS", 13f, forest, true))

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
            val categoryButton = button(
                category,
                if (selectedCategory == category) green else forest
            )

            categoryButton.setOnClickListener {
                selectedCategory = category
                showHomeScreen()
            }

            categoryRow.addView(
                categoryButton,
                LinearLayout.LayoutParams(-2, -2).apply {
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
                15f,
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
            LinearLayout.LayoutParams(-1, 0, 1f)
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
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        setContentView(root)

        searchInput.setText(searchText)

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

        card.addView(text(value, 22f, forest, true))
        card.addView(text(label, 10f, muted, true))

        return card
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
            dishContainer.addView(
                text(
                    "No dishes found. Add your first dish.",
                    14f,
                    muted
                )
            )
            return
        }

        filtered.forEach { dish ->
            dishContainer.addView(dishCard(dish))
        }
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

        details.addView(text(dish.name, 17f, forest, true))
        details.addView(text(dish.category, 12f, muted))
        details.addView(text("OMR ${dish.price}", 15f, green, true))

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

        if (dish.image.isNotBlank()) {
            try {
                val imageView = ImageView(this)
                imageView.setImageURI(Uri.parse(dish.image))
                imageView.adjustViewBounds = true
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP

                card.addView(
                    imageView,
                    LinearLayout.LayoutParams(-1, 180)
                )
            } catch (_: Exception) {}
        }

        if (dish.ingredients.isNotBlank()) {
            card.addView(
                text("Ingredients: ${dish.ingredients}", 12f, muted)
            )
        }

        if (dish.gsa.isNotBlank()) {
            card.addView(
                text("GSA: ${dish.gsa}", 12f, muted)
            )
        }

        if (dish.allergens.isNotBlank()) {
            card.addView(
                text("Allergens: ${dish.allergens}", 12f, muted)
            )
        }

        card.setOnClickListener {
            showDishOptions(dish)
        }

        card.layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
            bottomMargin = 10
        }

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
            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

            setText(existing?.price ?: "")
        }

        val categorySpinner = Spinner(this)
        val categoryOptions = categories.toMutableList()

        if (
            existing != null &&
            !categoryOptions.contains(existing.category)
        ) {
            categoryOptions.add(existing.category)
        }

        categorySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categoryOptions
        )

        if (existing != null) {
            val position = categoryOptions.indexOf(existing.category)

            if (position >= 0) {
                categorySpinner.setSelection(position)
            }
        }

        fields.addView(nameInput)
        fields.addView(priceInput)
        fields.addView(
            text("Select menu section", 13f, forest, true)
        )
        fields.addView(categorySpinner)

        val dialog = AlertDialog.Builder(this)
            .setTitle(
                if (existing == null) "Add Dish"
                else "Edit Dish"
            )
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

                    if (categoryOptions.isEmpty()) {
                        Toast.makeText(
                            this,
                            "Create a menu section first",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    if (existing == null) {
                        dishes.add(
                            Dish(
                                id = UUID.randomUUID().toString(),
                                name = name,
                                price = price,
                                category = categorySpinner.selectedItem.toString()
                            )
                        )
                    } else {
                        existing.name = name
                        existing.price = price
                        existing.category =
                            categorySpinner.selectedItem.toString()
                    }

                    saveData()
                    dialog.dismiss()
                    showHomeScreen()
                }
        }

        dialog.show()
    }

    private fun editInformation(
        dish: Dish,
        type: String
    ) {
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
                    "Ingredients" ->
                        dish.ingredients = input.text.toString()

                    "GSA" ->
                        dish.gsa = input.text.toString()

                    "Allergens" ->
                        dish.allergens = input.text.toString()
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
            .setMessage(
                "Enter add-on name and price, one per line."
            )
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
            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

            setText(dish.price)
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
    }    private fun chooseFoodPicture(dish: Dish) {
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
            .setMessage(
                "Are you sure you want to delete ${dish.name}?"
            )
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

                } else if (
                    categories.any { it.equals(name, true) }
                ) {
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
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 10, 20, 10)
            addView(text("Select section", 13f, forest, true))
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
                    categories[index] = newName

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
            "Export Menu Backup",
            "Import Menu Backup"
        )

        AlertDialog.Builder(this)
            .setTitle("Backup & Restore")
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
                        // Continue even if persistent permission is unavailable.
                    }

                    pendingImageDish?.image = uri.toString()
                    saveData()

                    pendingImageDish = null
                    showHomeScreen()

                    Toast.makeText(
                        this,
                        "Food picture updated",
                        Toast.LENGTH_SHORT
                    ).show()

                } catch (e: Exception) {
                    pendingImageDish = null

                    Toast.makeText(
                        this,
                        "Could not load picture: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            EXPORT_REQUEST -> {
                try {
                    contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(
                            createBackup().toByteArray(Charsets.UTF_8)
                        )
                    } ?: throw Exception("Could not open backup file")

                    Toast.makeText(
                        this,
                        "Backup exported successfully",
                        Toast.LENGTH_LONG
                    ).show()

                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Export failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            IMPORT_REQUEST -> {
                try {
                    val content = contentResolver
                        .openInputStream(uri)
                        ?.bufferedReader()
                        ?.use { it.readText() }
                        ?: throw Exception("Could not read file")

                    if (content.isBlank()) {
                        throw Exception("Empty backup file")
                    }

                    val root = JSONObject(content)

                    val newCategories = mutableListOf<String>()
                    val newDishes = mutableListOf<Dish>()

                    val categoryArray =
                        root.getJSONArray("categories")

                    val dishArray =
                        root.getJSONArray("dishes")

                    for (i in 0 until categoryArray.length()) {
                        newCategories.add(
                            categoryArray.getString(i)
                        )
                    }

                    for (i in 0 until dishArray.length()) {
                        val item = dishArray.getJSONObject(i)

                        newDishes.add(
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

                    AlertDialog.Builder(this)
                        .setTitle("Restore Backup")
                        .setMessage(
                            "This will replace your current menu with the imported backup. Continue?"
                        )
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Restore") { _, _ ->

                            categories.clear()
                            categories.addAll(newCategories)

                            dishes.clear()
                            dishes.addAll(newDishes)

                            selectedCategory = "All"
                            searchText = ""

                            saveData()
                            showHomeScreen()

                            Toast.makeText(
                                this,
                                "Backup restored successfully",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        .show()

                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Invalid backup file: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
