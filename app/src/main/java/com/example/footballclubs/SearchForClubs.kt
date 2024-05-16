package com.example.footballclubs

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.example.footballclubs.data.Clubs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL


class SearchForClubs : ComponentActivity() {
    var clubNameList = mutableListOf<String>()
    var clubImgList = mutableListOf<Bitmap?>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainContent()
        }
    }

    @Composable
    private fun MainContent() {

        var inputText: String by rememberSaveable {
            mutableStateOf("")
        }

        var isImgShown by rememberSaveable {
            mutableStateOf(
                false
            )
        }
        val scope = rememberCoroutineScope()
        LazyColumn {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = inputText,
                        onValueChange = {
                            inputText = it

                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White,
                            focusedIndicatorColor = Color(0xFF2894F4),
                            unfocusedIndicatorColor = Color(0xFF2894F4),
                            focusedLabelColor = Color(0xFF2894F4),
                            unfocusedLabelColor = Color(0xFF2894F4),
                        ),

                        label = { Text(text = "Search") },
                        maxLines = 1,


                        trailingIcon = {
                            if (!inputText.equals("")) {
                                Icon(
                                    imageVector = Icons.Outlined.Clear,
                                    contentDescription = null,
                                    modifier = Modifier.clickable {
                                        inputText = "" // Make text field empty
                                    }
                                )
                            }
                        }
                    )
                    Button(

                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                clubNameList.clear()
                                clubImgList.clear()
                                processInfoToDisplay(inputText)
                                inputText = ""
                                isImgShown = true

                            }
                        },
                        colors = ButtonDefaults.buttonColors(Color(0xFF2894F4)),

                        ) {
                        Text(text = "Search ", color = Color.White)
                    }

                    if (isImgShown) {
                        displayInfo()
                    }
                }
            }
        }
    }

    @Composable
    private fun displayInfo() {
        for (i in 0 until clubNameList.size) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 10.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .shadow(ambientColor = Color.DarkGray, elevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White, //Card background color
                    contentColor = Color.Black  //Card content color
                ),

                shape = RoundedCornerShape(8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(text = clubNameList[i])

                    clubImgList[i]?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Club Logo",
                            modifier = Modifier
                                .size(100.dp)
                        )
                    }


                }
            }
        }
        clubNameList.clear()
        clubImgList.clear()
    }

    private suspend fun processInfoToDisplay(inputText: String) {


        val listOfClubs: List<Clubs> = clubsDao.searchClubsByClubNameOrLeague(inputText)

        for (club in listOfClubs) {
            clubNameList.add(
                " Club Name: ${club.name}\n" +
                        "logo: ${club.teamLogo}\n" +
                        "\n"
            )

//            var x = club?.teamLogo
            if (club.teamLogo != "null") {
                var url = URL(club.teamLogo)
                var bmp: Bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                clubImgList.add(bmp)
            } else clubImgList.add(null)
        }

    }

}

