package com.example.doors

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.Observer
import com.example.doors.models.MaintenanceViolationModel
import com.example.doors.retrofit.RetrofitHelper
import com.example.doors.ui.theme.DoorsTheme
import com.example.doors.viewmodels.CityDataViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

class MainActivity : ComponentActivity() {
    

    override fun onCreate(savedInstanceState: Bundle?) {
//        cityDataViewModel.data.observe(this,  {
//            RecyclerView(it)
//        })
        super.onCreate(savedInstanceState)
        setContent {
            DoorsTheme {
                // A surface container using the 'background' color from the theme
                UserInput()
                loadData()

            }
        }
    }
}
var load = false
val cityDataViewModel = CityDataViewModel()
@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
@Composable
fun UserInput() {
    var streetNum by remember { mutableStateOf(TextFieldValue("")) }
    var streetName by remember { mutableStateOf(TextFieldValue("")) }
    var zipcode by remember { mutableStateOf(TextFieldValue("")) }
    var boro by remember { mutableStateOf(TextFieldValue("")) }

    Column {
        TextField(
            value = streetNum,
            onValueChange = { newText -> streetNum = newText },
            label = { Text(text = "Street Number") },
            placeholder = {
                Text(
                    text = "Enter Street Number"
                )
            }
        )
        TextField(
            value = streetName,
            onValueChange = { newText -> streetName = newText },
            label = { Text(text = "Street Name") },
            placeholder = {
                Text(
                    text = "Enter Street Name"
                )
            }
        )
        TextField(
            value = zipcode,
            onValueChange = { newText -> zipcode = newText },
            label = { Text(text = "Zipcode") },
            placeholder = {
                Text(
                    text = "Enter Zipcode"
                )
            }
        )
        TextField(
            value = boro,
            onValueChange = { newText -> boro = newText },
            label = { Text(text = "Boro") },
            placeholder = {
                Text(
                    text = "Enter Boro"
                )
            }
        )
        Button(onClick = { call(streetNum.text, streetName.text, boro.text, zipcode.text)
        }) {
            Text(text = "Search")

        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun call(streetNum: String, streetName: String, boro: String, zipcode: String) {
    Log.d("TAG", "call: asdf")
    val quotesApi = RetrofitHelper.getInstance().create(CityDataApi::class.java)
// launching a new coroutine
//    GlobalScope.launch(Dispatchers.Main) {
//        cityDataViewModel.getData(streetNum, streetName, boro, zipcode)

    GlobalScope.launch {
        val result = quotesApi.getData(streetNum, streetName, boro, zipcode)
        Log.d("TAG", "call: ${result.body()}")
        if (result.isSuccessful) {
            Log.d("TAG", "call: ${result.body()}")
            load = true
//            ListItem(result.body()!!)
        }
    }
    }

@Composable
fun loadData() {
    var newData by remember { mutableStateOf(TextFieldValue("")) }
    if (load) {
        Column {
            TextField(
                value = newData,
                onValueChange = { newText -> newData = newText },
                label = { Text(text = "Street") },
                placeholder = {
                    Text(
                        text = "Enter Street "
                    )
                }
            )
        }
    } else {

    }
}

var ItemsInList = mutableListOf<MaintenanceViolationModel>()
@Composable
fun ListItem(item: MaintenanceViolationModel) {
    val expanded = remember { mutableStateOf(false)}


    Surface(color = Color.Cyan,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)){

        Column(modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()) {

            Row{

                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(text = "Course")
                    Text(text = item.inspectiondate.toString())
                }

                OutlinedButton(onClick = { expanded.value = !expanded.value }) {
                    Text(if (expanded.value) "Show less" else "Show more")
                }
            }

            if (expanded.value){

                Column {
                    Text(text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.")
                }

            }
        }

    }
}


//@Composable
//fun RecyclerView(names : List<MaintenanceViolationModel>){
//
//    LazyColumn(modifier = Modifier.padding(vertical = 4.dp)){
//
//        items(items = names){ name ->
//
//            ListItem(name = name)
//
//        }
//
//    }
//
//}
//fun RecyclerView(items : List<MaintenanceViolationModel>){
//
//    LazyColumn(modifier = Modifier.padding(vertical = 4.dp)){
//
//        items(count = items.size){ int ->
//
//            ListItem(item)
//
//        }
//
//    }
//
//}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DoorsTheme {
    }
}
