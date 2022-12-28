package com.shakil.githubactionexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shakil.githubactionexample.ui.theme.GithubActionExampleTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GithubActionExampleTheme {
                Greeting()
            }
        }
    }
}

@Composable
fun Greeting() {
    Column(Modifier.fillMaxSize()) {
        val list by remember { mutableStateOf((1..10).toList()) }
        val map = rememberMap(list.size)
        val state = rememberLazyListState()
        var focusIndex by remember { mutableStateOf(0) }
        val scope = rememberCoroutineScope()
        ToolBarButton(focusIndex, state, list.size)
        LazyColumn(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), content = {
                itemsIndexed(list) { index, _ ->
                    Content(state, map, index, list) { fIndex ->
                        focusIndex = fIndex
                        scope.launch {
                            state.scrollToItem(fIndex)
                        }
                    }
                }
                item {
                    Text("Some Text")
                }
            })
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ToolBarButton(focusIndex: Int, state: LazyListState, size: Int) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    Row(Modifier.fillMaxWidth()) {
        Button(onClick = {
            scope.launch {
                val newIndex = focusIndex - 1
                if (newIndex < 0) {
                    state.scrollToItem(size - 1)
                } else {
                    state.scrollToItem(newIndex)
                }
                delay(200)
                focusManager.moveFocus(FocusDirection.Up)
            }

        }) {
            Text("UPS")

        }
        Button(onClick = {
            scope.launch {
                val newIndex = focusIndex + 1
                if (newIndex == size) {
                    state.scrollToItem(0)
                } else {
                    state.scrollToItem(newIndex)
                }
                delay(200)
                focusManager.moveFocus(FocusDirection.Down)
            }
        }) {
            Text("DOWN")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Content(
    state: LazyListState,
    map: Map<Int, FocusRequester>,
    index: Int,
    list: List<Int>,
    onFocusChange: (Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    Row(
        Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        var text1 by remember { mutableStateOf(TextFieldValue("${index}")) }
        var text2 by remember { mutableStateOf(TextFieldValue("${list.size + index}")) }

        TextField(modifier = Modifier
            .weight(1f)
            .height(200.dp)
            .focusRequester(getFocusRequester(map, index))
            .focusProperties {
                down = if (index == list.lastIndex) getFocusRequester(map, 0)
                else getFocusRequester(map, index + 1)
                up = if (index == 0) getFocusRequester(map, list.lastIndex)
                else getFocusRequester(map, index - 1)
            }
            .onFocusEvent { focusState ->
                if (focusState.isFocused) {
                    onFocusChange(index)
                }
            },
            value = text1, onValueChange = { newText ->
                text1 = newText
            })
        Spacer(modifier = Modifier.width(10.dp))
        TextField(modifier = Modifier
            .weight(1f)
            .focusRequester(getFocusRequester(map, list.size + index))
            .focusProperties {
                down = if (index == list.lastIndex) getFocusRequester(
                    map,
                    list.size
                ) else getFocusRequester(map, list.size + index + 1)
                up = if (index == 0) getFocusRequester(
                    map,
                    list.size + list.lastIndex
                ) else getFocusRequester(map, list.size + index - 1)
            }
            .onFocusEvent { focusState ->
                if (focusState.isFocused) {
                    onFocusChange(index)
                }
            },
            value = text2, onValueChange = { newText ->
                text2 = newText
            })
    }
}

private fun getFocusRequester(
    map: Map<Int, FocusRequester>,
    index: Int
) = map.getOrDefault(index, FocusRequester.Default)


@Composable
fun rememberMap(size: Int): Map<Int, FocusRequester> {
    return remember(size) {
        (0 until (size * 2)).associateWith { FocusRequester() }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GithubActionExampleTheme {
        Greeting()
    }
}