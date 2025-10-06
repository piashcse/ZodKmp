package com.piashcse.zodkmp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.outlined.AdsClick
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ControlPoint
import androidx.compose.material.icons.outlined.Dataset
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.JoinFull
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Reorder
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.ToggleOn
import androidx.compose.material.icons.outlined.Transform
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZodKmpModernExample() {
    var selectedTab by remember { mutableStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ZodKmp",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text("Basics") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Category, contentDescription = null) },
                    label = { Text("Advanced") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Science, contentDescription = null) },
                    label = { Text("Transform") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> BasicValidationScreen()
                1 -> AdvancedValidationScreen()
                2 -> TransformationScreen()
            }
        }
    }
}

@Composable
fun BasicValidationScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeaderCard(
                title = "Basic Validation",
                description = "Fundamental data validation with ZodKmp",
                icon = Icons.Filled.CheckCircle
            )
        }
        
        item {
            StringValidationCard()
        }
        
        item {
            NumberValidationCard()
        }
        
        item {
            BooleanValidationCard()
        }
        
        item {
            LiteralValidationCard()
        }
        
        item {
            EnumValidationCard()
        }
        
        item {
            FooterSpacer()
        }
    }
}

@Composable
fun AdvancedValidationScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeaderCard(
                title = "Advanced Validation",
                description = "Complex data structures and schemas",
                icon = Icons.Outlined.Category
            )
        }
        
        item {
            ArrayValidationCard()
        }
        
        item {
            ObjectValidationCard()
        }
        
        item {
            UnionValidationCard()
        }
        
        item {
            TupleValidationCard()
        }
        
        item {
            RecordValidationCard()
        }
        
        item {
            NullableValidationCard()
        }
        
        item {
            FooterSpacer()
        }
    }
}

@Composable
fun TransformationScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeaderCard(
                title = "Transformations & Refinements",
                description = "Modify and enhance your data validation",
                icon = Icons.Outlined.Science
            )
        }
        
        item {
            TransformValidationCard()
        }
        
        item {
            RefineValidationCard()
        }
        
        item {
            DefaultValidationCard()
        }
        
        item {
            ConditionalValidationCard()
        }
        
        item {
            FooterSpacer()
        }
    }
}

@Composable
fun HeaderCard(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(48.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ValidationCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            content()
        }
    }
}

@Composable
fun StringValidationCard() {
    ValidationCard(
        title = "String Validation",
        icon = Icons.Outlined.TextFields
    ) {
        var text by remember { mutableStateOf("hello@example.com") }
        var validationResult by remember { mutableStateOf<ZodResult<String>?>(null) }
        
        val emailSchema = Zod.string().email()
        
        Column {
            OutlinedTextField(
                value = text,
                onValueChange = { 
                    text = it
                    validationResult = emailSchema.safeParse(it)
                },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = validationResult is ZodResult.Failure
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ValidationStatusDisplay(validationResult)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Try: john@example.com or invalid-email",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun NumberValidationCard() {
    ValidationCard(
        title = "Number Validation",
        icon = Icons.Outlined.Numbers
    ) {
        var text by remember { mutableStateOf("25") }
        var validationResult by remember { mutableStateOf<ZodResult<Double>?>(null) }
        
        val ageSchema = Zod.number().min(0.0).max(120.0)
        
        Column {
            OutlinedTextField(
                value = text,
                onValueChange = { 
                    text = it
                    val number = try { it.toDouble() } catch (e: Exception) { null }
                    validationResult = if (number != null) ageSchema.safeParse(number) else ZodResult.Failure(ZodError(listOf("Invalid number")))
                },
                label = { Text("Age (0-120)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = validationResult is ZodResult.Failure
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ValidationStatusDisplay(validationResult)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Try: 25, -5, or 150",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun BooleanValidationCard() {
    ValidationCard(
        title = "Boolean Validation",
        icon = Icons.Outlined.ToggleOn
    ) {
        var isChecked by remember { mutableStateOf(true) }
        var validationResult by remember { mutableStateOf<ZodResult<Boolean>?>(null) }
        
        val booleanSchema = Zod.boolean()
        
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Switch(
                    checked = isChecked,
                    onCheckedChange = { 
                        isChecked = it
                        validationResult = booleanSchema.safeParse(it)
                    }
                )
                Text(
                    text = if (isChecked) "Enabled" else "Disabled",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ValidationStatusDisplay(validationResult)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Toggle the switch to see validation in action",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun LiteralValidationCard() {
    ValidationCard(
        title = "Literal Validation",
        icon = Icons.Filled.Label
    ) {
        var text by remember { mutableStateOf("active") }
        var validationResult by remember { mutableStateOf<ZodResult<String>?>(null) }
        
        val statusSchema = Zod.literal("active")
        
        Column {
            OutlinedTextField(
                value = text,
                onValueChange = { 
                    text = it
                    validationResult = statusSchema.safeParse(it)
                },
                label = { Text("Status (must be 'active')") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = validationResult is ZodResult.Failure
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ValidationStatusDisplay(validationResult)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Only 'active' is valid",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun EnumValidationCard() {
    ValidationCard(
        title = "Enum Validation",
        icon = Icons.Outlined.List
    ) {
        var selectedRole by remember { mutableStateOf("user") }
        var validationResult by remember { mutableStateOf<ZodResult<String>?>(null) }
        
        val roleSchema = Zod.enum("admin", "user", "guest")
        
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("admin", "user", "guest").forEach { role ->
                    FilterChip(
                        selected = selectedRole == role,
                        onClick = {
                            selectedRole = role
                            validationResult = roleSchema.safeParse(role)
                        },
                        label = { Text(role.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ValidationStatusDisplay(validationResult)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Select a role from the chips above",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun ArrayValidationCard() {
    ValidationCard(
        title = "Array Validation",
        icon = Icons.Outlined.ViewList
    ) {
        var tags by remember { mutableStateOf("tag1,tag2,tag3") }
        var validationResult by remember { mutableStateOf<ZodResult<List<String>>?>(null) }
        
        val tagsSchema = Zod.array(Zod.string().min(2)).min(1).max(5)
        
        Column {
            OutlinedTextField(
                value = tags,
                onValueChange = { 
                    tags = it
                    val tagList = it.split(",").map { tag -> tag.trim() }.filter { it.isNotEmpty() }
                    validationResult = tagsSchema.safeParse(tagList)
                },
                label = { Text("Tags (comma separated, 1-5 items, min 2 chars each)") },
                modifier = Modifier.fillMaxWidth(),
                isError = validationResult is ZodResult.Failure
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ValidationStatusDisplay(validationResult)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Try: tag1,tag2 or tag (too short)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun ObjectValidationCard() {
    ValidationCard(
        title = "Object Validation",
        icon = Icons.Outlined.Dataset
    ) {
        var name by remember { mutableStateOf("John Doe") }
        var email by remember { mutableStateOf("john@example.com") }
        var age by remember { mutableStateOf("30") }
        var validationResult by remember { mutableStateOf<ZodResult<Map<String, Any?>>?>(null) }
        
        val userSchema = Zod.objectSchema<Map<String, Any?>>({
            string("name", Zod.string().min(2))
            string("email", Zod.string().email())
            number("age", Zod.number().min(0.0).max(120.0))
        }) { map -> map }
        
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { 
                    name = it
                    validateUserObject(userSchema, name, email, age) { result -> validationResult = result }
                },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    validateUserObject(userSchema, name, email, age) { result -> validationResult = result }
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = age,
                onValueChange = { 
                    age = it
                    validateUserObject(userSchema, name, email, age) { result -> validationResult = result }
                },
                label = { Text("Age") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ValidationStatusDisplay(validationResult)
        }
    }
}

fun validateUserObject(
    schema: ZodObjectSchema<Map<String, Any?>>,
    name: String,
    email: String,
    age: String,
    onResult: (ZodResult<Map<String, Any?>>) -> Unit
) {
    val ageValue = try { age.toDouble() } catch (e: Exception) { null }
    if (ageValue != null) {
        val userData = mapOf(
            "name" to name,
            "email" to email,
            "age" to ageValue
        )
        onResult(schema.safeParse(userData))
    }
}

@Composable
fun UnionValidationCard() {
    ValidationCard(
        title = "Union Validation",
        icon = Icons.Outlined.JoinFull
    ) {
        var inputValue by remember { mutableStateOf("hello") }
        var validationResult by remember { mutableStateOf<ZodResult<Any?>?>(null) }
        
        val stringOrNumberSchema = Zod.union(Zod.string(), Zod.number())
        
        Column {
            OutlinedTextField(
                value = inputValue,
                onValueChange = { 
                    inputValue = it
                    val parsedValue = try { 
                        if (it.toDoubleOrNull() != null) it.toDouble() else it 
                    } catch (e: Exception) { it }
                    validationResult = stringOrNumberSchema.safeParse(parsedValue)
                },
                label = { Text("String or Number") },
                modifier = Modifier.fillMaxWidth(),
                isError = validationResult is ZodResult.Failure
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ValidationStatusDisplay(validationResult)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Try: hello or 42",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun TupleValidationCard() {
    ValidationCard(
        title = "Tuple Validation",
        icon = Icons.Outlined.Reorder
    ) {
        var x by remember { mutableStateOf("10") }
        var y by remember { mutableStateOf("20") }
        var validationResult by remember { mutableStateOf<ZodResult<List<Any?>>?>(null) }
        
        val coordinateSchema = Zod.tuple(listOf(Zod.number(), Zod.number()))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = x,
                    onValueChange = { 
                        x = it
                        validateCoordinate(coordinateSchema, x, y) { result -> validationResult = result }
                    },
                    label = { Text("X Coordinate") },
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = y,
                    onValueChange = { 
                        y = it
                        validateCoordinate(coordinateSchema, x, y) { result -> validationResult = result }
                    },
                    label = { Text("Y Coordinate") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            ValidationStatusDisplay(validationResult)
        }
    }
}

fun validateCoordinate(
    schema: ZodTuple,
    x: String,
    y: String,
    onResult: (ZodResult<List<Any?>>) -> Unit
) {
    val xValue = try { x.toDouble() } catch (e: Exception) { null }
    val yValue = try { y.toDouble() } catch (e: Exception) { null }
    
    if (xValue != null && yValue != null) {
        val coordinate = listOf(xValue, yValue)
        onResult(schema.safeParse(coordinate))
    }
}

@Composable
fun RecordValidationCard() {
    ValidationCard(
        title = "Record Validation",
        icon = Icons.Outlined.Key
    ) {
        var validationResult by remember { mutableStateOf<ZodResult<Map<String, String>>?>(null) }
        
        val recordSchema = Zod.record(Zod.string())
        
        Column {
            Text(
                text = "Records validate objects with string keys",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val sampleRecord = mapOf("firstName" to "John", "lastName" to "Doe")
            val result = recordSchema.safeParse(sampleRecord)
            
            ValidationStatusDisplay(result)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Sample record validated automatically",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun NullableValidationCard() {
    ValidationCard(
        title = "Nullable Validation",
        icon = Icons.Outlined.HelpOutline
    ) {
        var text by remember { mutableStateOf("") }
        var validationResult by remember { mutableStateOf<ZodResult<String?>?>(null) }
        
        val nullableSchema = Zod.string().nullable()
        
        Column {
            OutlinedTextField(
                value = text,
                onValueChange = { 
                    text = it
                    val value = if (it.isEmpty()) null else it
                    validationResult = nullableSchema.safeParse(value)
                },
                label = { Text("Nullable String (can be empty)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ValidationStatusDisplay(validationResult)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Leave empty to test null value",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun TransformValidationCard() {
    ValidationCard(
        title = "Transform Validation",
        icon = Icons.Outlined.Transform
    ) {
        var text by remember { mutableStateOf("hello world") }
        var validationResult by remember { mutableStateOf<ZodResult<String>?>(null) }
        
        val uppercaseSchema = Zod.string().transform { it.uppercase() }
        
        Column {
            OutlinedTextField(
                value = text,
                onValueChange = { 
                    text = it
                    validationResult = uppercaseSchema.safeParse(it)
                },
                label = { Text("Text to Uppercase") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            validationResult?.let { result ->
                when (result) {
                    is ZodResult.Success -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Transformed: ${result.data}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    is ZodResult.Failure -> {
                        ErrorMessage(result.error.errors.joinToString(", "))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Enter text to see it transformed to uppercase",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun RefineValidationCard() {
    ValidationCard(
        title = "Refinement Validation",
        icon = Icons.Outlined.AdsClick
    ) {
        var text by remember { mutableStateOf("42") }
        var validationResult by remember { mutableStateOf<ZodResult<Double>?>(null) }
        
        val evenNumberSchema = Zod.number().refine({ it.toInt() % 2 == 0 }) { "Number must be even" }
        
        Column {
            OutlinedTextField(
                value = text,
                onValueChange = { 
                    text = it
                    val number = try { it.toDouble() } catch (e: Exception) { null }
                    validationResult = if (number != null) evenNumberSchema.safeParse(number) else ZodResult.Failure(ZodError(listOf("Invalid number")))
                },
                label = { Text("Even Number") },
                modifier = Modifier.fillMaxWidth(),
                isError = validationResult is ZodResult.Failure
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ValidationStatusDisplay(validationResult)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Try: 42 (even) or 43 (odd)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun DefaultValidationCard() {
    ValidationCard(
        title = "Default Values",
        icon = Icons.Outlined.Restore
    ) {
        var text by remember { mutableStateOf("") }
        var validationResult by remember { mutableStateOf<ZodResult<String>?>(null) }
        
        val stringWithDefault = Zod.string().default("default_value")
        
        Column {
            OutlinedTextField(
                value = text,
                onValueChange = { 
                    text = it
                    val value = if (it.isEmpty()) null else it
                    validationResult = stringWithDefault.safeParse(value)
                },
                label = { Text("String (empty = default)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            validationResult?.let { result ->
                when (result) {
                    is ZodResult.Success -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Value: '${result.data}'",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    is ZodResult.Failure -> {
                        ErrorMessage(result.error.errors.joinToString(", "))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Leave empty to see default value applied",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun ConditionalValidationCard() {
    ValidationCard(
        title = "Conditional Validation",
        icon = Icons.Outlined.ControlPoint
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Science,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Advanced Conditional Validation",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Combine multiple schemas with conditional logic",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "✓ Complex validation rules\n✓ Cross-field validation\n✓ Dynamic schema selection",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ValidationStatusDisplay(result: ZodResult<*>?) {
    result?.let { res ->
        when (res) {
            is ZodResult.Success -> {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Valid input",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
            is ZodResult.Failure -> {
                ErrorMessage(res.error.errors.joinToString(", "))
            }
        }
    }
}

@Composable
fun ErrorMessage(message: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun FooterSpacer() {
    Spacer(modifier = Modifier.height(32.dp))
}