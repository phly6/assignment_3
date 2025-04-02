/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.tiptime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tiptime.ui.theme.TipTimeTheme
import java.text.NumberFormat
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.annotation.StringRes
import androidx.compose.ui.text.input.ImeAction

class MainActivity : ComponentActivity() {  //Defines main activity & extends ComponentActivity
    override fun onCreate(savedInstanceState: Bundle?) {    //Sets up content when app is started. Function that is part of Android Activity Lifecycle. Called when program is first created.SavedInstanceState is a nullable parameter that can restore the app's previous state or be null.
        enableEdgeToEdge()  //Makes app draw behind system bars like status bar on top or nav bar on bottom. Tells program you will handle padding.
        super.onCreate(savedInstanceState)  //Important function that tells the Android framework to do its normal functions when creating an app, even though it was overridden
        setContent {    //Defines the UI with setContent
            TipTimeTheme {  //Wrap for custom theme
                Surface(    //Provides a base layer for the UI to be built upon
                    modifier = Modifier.fillMaxSize(),
                ) {
                    TipTimeLayout() //Calls TipTimeLayout composable to build main screen
                }
            }
        }
    }
}

@Composable
fun EditNumberField(
    @StringRes label: Int,  //Tells android tools this label should only ever use a string resource (safety check)
    keyboardOptions: KeyboardOptions,   //Sets keyboard type and what the keyboard action button should be
    value: String,  //Tells the UI to display the value in the text box
    onValueChange: (String) -> Unit,    //Defines function parameter that expects a lambda with a string input but no return value. Basically updates the variable
    modifier: Modifier = Modifier) {    //Basically allows you to use a modifier instead of android using a blank default
    var amountInput by remember { mutableStateOf("") }  //Creates a variable that will be mutated when changed but remains across reconstructions of the UI

    val amount = amountInput.toDoubleOrNull() ?: 0.0    //Creates a value (amount) and parses it to double or makes it null
    val tip = calculateTip(amount)  //Creates a value (tip) and tells it to store the result from calculateTip

    TextField(  //Defines the way text fields will be displayed locally
        value = value,  //Defines what to show in the text field
        onValueChange = onValueChange,  //Passing a value through to the text field
        singleLine = true,  //Prevents the user from entering multiple lines. Keeps it to one line
        label = {Text(stringResource(label))},  //Defining how labels will appear and where it will get its resources from
        keyboardOptions = keyboardOptions,  //Defines keyboard behavior
        modifier = modifier //Passing modifier in to define appearance

    )
}
@Composable
fun TipTimeLayout() {   //Building main layout
    var tipInput by remember {mutableStateOf("")}   //Stores tip input
    val tipPercent = tipInput.toDoubleOrNull() ?:0.0    //Parses tip input to double
    var amountInput by remember { mutableStateOf("") }  //Stores amount input
    val amount = amountInput.toDoubleOrNull() ?: 0.0    //Parses amount input to double
    val tip = calculateTip(amount, tipPercent)  //Calls calculateTip function and passes amount & tipPercent

    Column( //Puts a column around content
        modifier = Modifier //Pass modifier
            .statusBarsPadding()    //This adds padding at the top equal to the height of the status bar so content isnt lost behind it from enableEdgetoEdge()
            .padding(horizontal = 40.dp)    //adds padding to left and right of the outside of the column
            .safeDrawingPadding(),  //This adds padding for all areas considered unsafe (like cutouts or rounded corners)
        horizontalAlignment = Alignment.CenterHorizontally, //Centers content from left and right
        verticalArrangement = Arrangement.Center    //Centers content from top and bottom
    ) {
        Text(   //Title text
            text = stringResource(R.string.calculate_tip),  //Gets calculate_tip string from strings.xml
            modifier = Modifier //Pass modifier
                .padding(bottom = 16.dp, top = 40.dp)   //Adds padding to bottom and top
                .align(alignment = Alignment.Start) //Aligns to ?
        )
        EditNumberField(    //Text field for tip percent
            label = R.string.bill_amount,   //Gets bill_amount string from strings.xml
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            value = amountInput,
            onValueChange = { amountInput = it },
            modifier = Modifier
                .padding(bottom = 32.dp)
                .fillMaxWidth() //Make this content as big as possible inside its parent
        )
        EditNumberField(    //Adds tip percentage field
            label = R.string.how_was_the_service,   //Gets how_was_the_service string from strings.xml
            keyboardOptions = KeyboardOptions.Default.copy( //Create a copy of the default keyboard options and edit just the parts you want
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done  //Sets the action button on the keyboard to finish typing
            ),
            value = tipInput,
            onValueChange = {tipInput = it},
            modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth()
        )
        Text(   //Displays tip_amount as money
            text = stringResource(R.string.tip_amount, tip),
            style = MaterialTheme.typography.displaySmall   //Predefined theme
        )
        Spacer(modifier = Modifier.height(150.dp))  //Adds space at the bottom
    }
}

/**
 * Calculates the tip based on the user input and format the tip amount
 * according to the local currency.
 * Example would be "$10.00".
 */

//Function to calculate the tip
private fun calculateTip(amount: Double, tipPercent: Double = 15.0): String { //Define private function and input 2 doubles Set default tip to 15%. Return a string
    val tip = tipPercent / 100 * amount //Define tip as result of tip calculation
    return NumberFormat.getCurrencyInstance().format(tip)   //Return calculated tip
}

//Preview composable to preview UI before running emulator
@Preview(showBackground = true)
@Composable
fun TipTimeLayoutPreview() {
    TipTimeTheme {
        TipTimeLayout()
    }
}
