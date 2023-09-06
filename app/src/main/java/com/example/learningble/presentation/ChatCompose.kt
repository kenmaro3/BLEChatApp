package com.example.learningble.presentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import java.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.material.LocalAbsoluteElevation
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage

import com.example.learningble.bluetooth.ChatServer
import com.example.learningble.models.Message

private const val TAG = "ChatCompose"

fun convertImageByteArrayToBitmap(imageData: ByteArray): Bitmap {
    return BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
}

@Composable
fun BitmapImage(bitmap: Bitmap) {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "some useful description",
    )
}
@RequiresApi(Build.VERSION_CODES.O)
fun ByteArray.toBase64(): String =
    String(Base64.getEncoder().encode(this))

object ChatCompose {


    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun ShowChat(message: Message) {
        Row(
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth(),
            horizontalArrangement = if (message is Message.RemoteMessage) Arrangement.Start else Arrangement.End
        ) {
            if (message is Message.RemoteMessage || message is Message.LocalMessage){
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .padding(5.dp)
                        .border(1.dp, Color.Black, shape = RoundedCornerShape(10.dp))
                        .background(
                            if (message is Message.RemoteMessage) Color(0xFFD3D3D3) else Color(
                                0xFF90EE90
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    Text(text = message.text, color = Color.Black, modifier = Modifier.padding(10.dp))
                }
            }else if(message is Message.RemoteImage || message is Message.LocalImage){
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .padding(5.dp)
                        .border(1.dp, Color.Black, shape = RoundedCornerShape(10.dp))
                        .background(
                            if (message is Message.RemoteImage) Color(0xFFD3D3D3) else Color(
                                0xFF90EE90
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    val byteArray = Base64.getDecoder().decode(message.text)
                    BitmapImage(convertImageByteArrayToBitmap(byteArray))
                    //AsyncImage(model = uri, contentDescription = null, modifier = Modifier.size(248.dp))
                }

            }
        }
    }

    @Composable
    fun Chats(deviceName: String?) {
        val message by ChatServer.messages.observeAsState()

        val inputvalue = remember { mutableStateOf(TextFieldValue()) }

        val messageList = remember {
            mutableStateListOf<Message>()
        }

        if (message != null && !messageList.contains(message)) {
            messageList.add(message!!)
        }



        if (messageList.isNotEmpty()) {
            Log.d("DEBUG1", messageList.toString())
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Chat Now with ${deviceName ?: "Unknown"}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                Surface(modifier = Modifier
                    .padding(all = Dp(5f))
                    .fillMaxHeight(fraction = 0.85f)) {
                    ChatsList(messageList)
                }


                InputField(inputvalue)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(fraction = 0.85f)
                ) {
                    Text(text = "No Chat History")
                }
                
                InputField(inputvalue = inputvalue)
            }
        }
    }

    @Composable
    fun InputField(inputvalue: MutableState<TextFieldValue>){
        var uri by remember{
            mutableStateOf<Uri?>(null)
        }

        val context = LocalContext.current

        val singlePhotoPicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = {
                uri = it
            }
        )

        Column(
            Modifier
                .fillMaxWidth()
        ) {

            Row(){
                Button(onClick = {
                    singlePhotoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )

                }){
                    Text("Image")
                }

                TextField(
                    value = inputvalue.value,
                    onValueChange = {
                        inputvalue.value = it
                    },
                    placeholder = { Text(text = "Enter your message") },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrect = true,
                        keyboardType = KeyboardType.Text,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    textStyle = TextStyle(
                        color = Color.Black, fontSize = TextUnit.Unspecified,
                        fontFamily = FontFamily.SansSerif
                    ),
                    maxLines = 1,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = {
                    if (inputvalue.value.text.isNotEmpty()) {
                        ChatServer.sendMessage(inputvalue.value.text)
                        inputvalue.value = TextFieldValue()
                    }else if(uri != null){
                        Log.d("DEBUG", "here called")
                        ChatServer.sendImage(uri!!, context)
                        uri = null
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Send", fontSize = 15.sp)
            }
        }
    }
    
    @Composable
    fun ChatsList(messagesList: List<Message>) {
        LazyColumn(modifier = Modifier.background(Color.White)) {
            items(count = messagesList.size) { index ->
                if (messagesList.isNotEmpty())
                    ShowChat(message = messagesList[index])
            }
        }
    }

}


@Preview(name = "name")
@Composable
fun DefaultPreview() {
    ChatCompose.Chats("test")
}