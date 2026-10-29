
package com.example.ibclearningapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

    private val handler = Handler(Looper.getMainLooper())

    private val defaultCategories = mutableListOf(
        "Salads", "Soups", "Starters", "Dynamites",
        "Baked Potatoes", "Pasta & Special", "Burgers",
        "Pizzas", "Desserts", "Fresh Juices", "Mojitos",
        "Milkshakes", "Smoothies", "Hot Coffees",
        "Cold Coffee", "Other Drinks", "L.T.O."
    )

    private val categories = mutableListOf<String>()
    private val dishes = mutableListOf<Dish>()

    private var selectedCategory = "All"
    private var searchText = ""
    private var pendingImageId = ""

    private lateinit var content: LinearLayout

    private val prefs by lazy {
        getSharedPreferences("IBC_MENU_DATA", MODE_PRIVATE)
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = forest
        window.navigationBarColor = forest

        loadData()
        showSplash()
    }

    private fun showSplash() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(24), dp(24), dp(24))
            setBackgroundColor(forest)
        }

        val star = text("✦", 38f, gold, true)
        val brand = text("I B C", 52f, cream, true).apply {
            letterSpacing = 0.12f
            typeface = Typeface.create("serif", Typeface.BOLD)
        }
        val line = View(this).apply {
            setBackgroundColor(gold)
        }
        val title = text("MENU MANAGEMENT", 17f, gold, true)
        val byline = text("BY ANMOL RAI", 12f, cream, true)

        root.addView(star)
        root.addView(brand)
        root.addView(line, LinearLayout.LayoutParams(dp(100), dp(2)).apply {
            topMargin = dp(18)
            bottomMargin = dp(18)
        })
        root.addView(title)
        root.addView(byline)

        setContentView(root)

        handler.postDelayed({
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
            gravity = Gravity.CENTER_VERTICAL
            if (bold) setTypeface(null, Typeface.BOLD)
        }
    }

    private fun loadData() {
        categories.clear()

        val savedCategories = prefs.getString("categories", null)

        if (savedCategories != null) {
            val array = JSONArray(savedCategories)
            for (i in 0 until array.length()) {
                categories.add(array.getString(i))
            }
        } else {
            categories.addAll(defaultCategories)
        }

        dishes.clear()

        val savedDishes = prefs.getString("dishes", "[]") ?: "[]"
        val array = JSONArray(savedDishes)

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)

            dishes.add(
                Dish(
                    id = obj.optString("id"),
                    name = obj.optString("name"),
                    price = obj.optString("price"),
                    category = obj.optString("category"),
                    ingredients = obj.optString("ingredients"),
                    gsa = obj.optString("gsa"),
                    allergens = obj.optString("allergens"),
                    addons = obj.optString("addons"),
                    image = obj.optString("image")
                )
            )
        }
    }

    private fun saveData() {
        val categoryArray = JSONArray()
        categories.forEach { categoryArray.put(it) }

        val dishArray = JSONArray()

        dishes.forEach { dish ->
            val obj = JSONObject()
            obj.put("id", dish.id)
            obj.put("name", dish.name)
            obj.put("price", dish.price)
            obj.put("category", dish.category)
            obj.put("ingredients", dish.ingredients)
            obj.put("gsa", dish.gsa)
            obj.put("allergens", dish.allergens)
            obj.put("addons", dish.addons)
            obj.put("image", dish.image)
            dishArray.put(obj)
        }

        prefs.edit()
            .putString("categories", categoryArray.toString())
            .putString("dishes", dishArray.toString())
            .apply()
    }

    private fun showHomeScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(cream)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(22), dp(22), dp(20))
            setBackgroundColor(forest)
        }

        val top = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
        }

        val logo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        }

        logo.addView(text("✦  IBC APP", 25f, gold, true))
        logo.addView(text("PREMIUM MENU MANAGEMENT", 10f, cream))

        val categoryButton = button("CATEGORIES", gold, forest)
        categoryButton.setOnClickListener { manageCategories() }

        top.addView(logo)
        top.addView(categoryButton)
        header.addView(top)

        val welcome = text("Your Menu, Beautifully Managed", 21f, cream, true)
        welcome.setPadding(0, dp(24), 0, dp(5))
        header.addView(welcome)

        header.addView(
            text("Organise your dishes and menu information", 13f, gold)
        )

        root.addView(header)

        val search = EditText(this).apply {
            hint = "Search dishes..."
            textSize = 15f
            singleLine = true
            setTextColor(ink)
            setHintTextColor(muted)
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setBackgroundColor(Color.WHITE)
        }

        val searchBox = LinearLayout(this).apply {
            setPadding(dp(18), dp(15), dp(18), dp(10))
            addView(search, LinearLayout.LayoutParams(-1, dp(50)))
        }

        root.addView(searchBox)

        val stats = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(18), dp(5), dp(18), dp(12))
        }

        val totalCard = statCard("TOTAL DISHES", dishes.size.toString())
        val categoryCard = statCard("CATEGORIES", categories.size.toString())

        stats.addView(totalCard, LinearLayout.LayoutParams(0, dp(78), 1f))
        stats.addView(categoryCard, LinearLayout.LayoutParams(0, dp(78), 1f).apply {
            leftMargin = dp(10)
        })

        root.addView(stats)

        val categoryTitle = text("EXPLORE CATEGORIES", 15f, forest, true)
        categoryTitle.setPadding(dp(20), dp(8), dp(20), dp(10))
        root.addView(categoryTitle)

        val categoryScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
        }

        val categoryRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(16), 0, dp(16), dp(12))
        }

        fun addCategoryChip(name: String) {
            val active = selectedCategory == name

            val chip = button(
                name,
                if (active) forest else Color.WHITE,
                if (active) gold else forest
            )

            chip.setOnClickListener {
                selectedCategory = name
                showHomeScreen()
            }

            categoryRow.addView(
                chip,
                LinearLayout.LayoutParams(-2, dp(42)).apply {
                    rightMargin = dp(8)
                }
            )
        }

        addCategoryChip("All")
        categories.forEach { addCategoryChip(it) }

        categoryScroll.addView(categoryRow)
        root.addView(categoryScroll)

        val dishHeader = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(20), dp(8), dp(20), dp(10))
        }

        val dishTitle = text("MENU ITEMS", 17f, forest, true)
        dishHeader.addView(dishTitle, LinearLayout.LayoutParams(0, -2, 1f))

        val addButton = button("+ ADD DISH", forest, gold)
        addButton.setOnClickListener { showDishEditor(null) }
        dishHeader.addView(addButton)

        root.addView(dishHeader)

        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), 0, dp(16), dp(24))
        }

        val filtered = dishes.filter { dish ->
            val categoryMatch =
                selectedCategory == "All" || dish.category == selectedCategory

            val searchMatch =
                dish.name.contains(searchText, true) ||
                dish.category.contains(searchText, true)

            categoryMatch && searchMatch
        }

        if (filtered.isEmpty()) {
            val empty = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(20), dp(40), dp(20), dp(40))
            }

            empty.addView(text("✦", 38f, gold, true).apply {
                gravity = Gravity.CENTER
            })
            empty.addView(text("No dishes yet", 20f, forest, true).apply {
                gravity = Gravity.CENTER
            })
            empty.addView(text("Add your first menu item to get started.", 13f, muted).apply {
                gravity = Gravity.CENTER
            })
            list.addView(empty)
        }

        filtered.forEach { dish ->
            list.addView(dishCard(dish))
        }

        scroll.addView(list)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        val bottom = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(10), dp(16), dp(10))
            setBackgroundColor(forest)
        }

        val backup = button("DATA BACKUP", gold, forest)
        backup.setOnClickListener { showBackupInfo() }

        val footer = text("IBC APP  •  BY ANMOL RAI", 10f, cream)
        footer.gravity = Gravity.CENTER

        bottom.addView(footer, LinearLayout.LayoutParams(0, -2, 1f))
        bottom.addView(backup)

        root.addView(bottom)

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchText = s?.toString() ?: ""
                refreshDishList(root, search.text.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        setContentView(root)
    }

    private fun refreshDishList(root: LinearLayout, query: String) {
        // Search is applied when the home screen is refreshed.
        searchText = query
        showHomeScreen()
    }

    private fun statCard(label: String, value: String): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE)
        }

        card.addView(text(value, 24f, forest, true).apply {
            gravity = Gravity.CENTER
        })

        card.addView(text(label, 10f, muted, true).apply {
            gravity = Gravity.CENTER
        })

        return card
    }

    private fun button(
        label: String,
        background: Int,
        foreground: Int
    ): Button {
        return Button(this).apply {
            text = label
            textSize = 11f
            isAllCaps = false
            setTextColor(foreground)
            setBackgroundColor(background)
            typeface = Typeface.DEFAULT_BOLD
            minHeight = dp(40)
            minimumHeight = dp(40)
            setPadding(dp(12), 0, dp(12), 0)
        }
    }

    private fun dishCard(dish: Dish): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(12), dp(8), dp(12))
            setBackgroundColor(Color.WHITE)
        }

        val image = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(76), dp(76))
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(Color.rgb(235, 231, 220))

            if (dish.image.isNotBlank()) {
                try {
                    setImageURI(Uri.parse(dish.image))
                } catch (_: Exception) {}
            }
        }

        card.addView(image)

        val details = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, dp(4), 0)
        }

        details.addView(text(dish.name, 16f, forest, true))
        details.addView(text(dish.category, 11f, muted))
        details.addView(text("OMR ${dish.price}", 14f, green, true))

        card.addView(details, LinearLayout.LayoutParams(0, -2, 1f))

        val menu = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_more)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(forest)
            layoutParams = LinearLayout.LayoutParams(dp(42), dp(48))
        }

        menu.setOnClickListener { showDishMenu(menu, dish) }
        card.addView(menu)

        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(10))
            addView(card)
        }

        return wrapper
    }

    private fun showDishMenu(anchor: View, dish: Dish) {
        val popup = PopupMenu(this, anchor)

        val options = listOf(
            "Ingredients",
            "GSA – Gold Standard Attribute",
            "Allergens",
            "Manage Add-ons",
            "Edit Dish",
            "Edit Price",
            "Change Food Picture",
            "Delete Dish"
        )

        options.forEachIndexed { index, option ->
            popup.menu.add(0, index, index, option)
        }

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                0 -> editInformation(dish, "Ingredients", dish.ingredients) {
                    dish.ingredients = it
                }
                1 -> editInformation(dish, "GSA – Gold Standard Attribute", dish.gsa) {
                    dish.gsa = it
                }
                2 -> editInformation(dish, "Allergens", dish.allergens) {
                    dish.allergens = it
                }
                3 -> editInformation(dish, "Manage Add-ons", dish.addons) {
                    dish.addons = it
                }
                4 -> showDishEditor(dish)
                5 -> editPrice(dish)
                6 -> chooseFoodPicture(dish)
                7 -> deleteDish(dish)
            }
            true
        }

        popup.show()
    }

    private fun editInformation(
        dish: Dish,
        title: String,
        current: String,
        update: (String) -> Unit
    ) {
        val input = EditText(this).apply {
            setText(current)
            hint = "Enter $title"
            minLines = 4
            gravity = Gravity.TOP
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }

        val container = LinearLayout(this).apply {
            setPadding(dp(20), dp(8), dp(20), 0)
            addView(input)
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(container)
            .setPositiveButton("SAVE") { _, _ ->
                update(input.text.toString())
                saveData()
                showHomeScreen()
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun showDishEditor(existing: Dish?) {
        val name = EditText(this).apply {
            hint = "Dish name"
            setText(existing?.name ?: "")
        }

        val price = EditText(this).apply {
            hint = "Price in OMR"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(existing?.price ?: "")
        }

        val category = EditText(this).apply {
            hint = "Category"
            setText(existing?.category ?: categories.firstOrNull() ?: "")
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
            addView(name)
            addView(price)
            addView(category)
        }

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Add New Dish" else "Edit Dish")
            .setView(layout)
            .setPositiveButton("SAVE", null)
            .setNegativeButton("CANCEL", null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val dishName = name.text.toString().trim()
                        val dishPrice = price.text.toString().trim()
                        val dishCategory = category.text.toString().trim()

                        if (dishName.isBlank() || dishPrice.isBlank() ||
                            dishCategory.isBlank()
                        ) {
                            Toast.makeText(
                                this@MainActivity,
                                "Enter dish name, price and category",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        }

                        if (!categories.contains(dishCategory)) {
                            categories.add(dishCategory)
                        }

                        if (existing == null) {
                            dishes.add(
                                Dish(
                                    id = System.currentTimeMillis().toString(),
                                    name = dishName,
                                    price = dishPrice,
                                    category = dishCategory
                                )
                            )
                        } else {
                            existing.name = dishName
                            existing.price = dishPrice
                            existing.category = dishCategory
                                                    }

                        saveData()
                        dismiss()
                        showHomeScreen()
                    }
                }
                show()
            }
    }
                      
