/*
 * Copyright (c) 2022 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * These materials have been reviewed and are updated as of 7/2022.
 */

package com.yourcompany.android.taskie.networking

import com.google.gson.Gson
import com.yourcompany.android.taskie.App
import com.yourcompany.android.taskie.model.Task
import com.yourcompany.android.taskie.model.UserProfile
import com.yourcompany.android.taskie.model.request.AddTaskRequest
import com.yourcompany.android.taskie.model.request.UserDataRequest
import com.yourcompany.android.taskie.model.response.GetTasksResponse
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Holds decoupled logic for all the API calls.
 */

const val BASE_URL = "https://taskie-rw.herokuapp.com"

class RemoteApi(private val remoteApiService: RemoteApiService) {

    private val gson = Gson()

  fun loginUser(userDataRequest: UserDataRequest, onUserLoggedIn: (String?, Throwable?) -> Unit) {
    onUserLoggedIn("token", null)
  }

  fun registerUser(userDataRequest: UserDataRequest, onUserCreated: (String?, Throwable?) -> Unit) {
      val body = RequestBody.create(
          MediaType.parse("application/json"), gson.toJson(userDataRequest)
      )

      remoteApiService.registerUser(body).enqueue(object : retrofit2.Callback<ResponseBody> {
          override fun onFailure(call: Call<ResponseBody>, error: Throwable) {
              onUserCreated(null, error)
          }

          override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
              val message = response.body()?.string()
              if (message == null) onUserCreated(null, NullPointerException("No response body!"))

              onUserCreated(message, null)
          }
      })


  }

  fun getTasks(onTasksReceived: (List<Task>, Throwable?) -> Unit) {
    remoteApiService.getNotes(App.getToken()).enqueue(object : retrofit2.Callback<ResponseBody?> {
        override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
            val jsonBody = response.body()?.string()

            if(jsonBody == null){
                onTasksReceived(emptyList(), NullPointerException("No data available"))
                return
            }

            val data = gson.fromJson(jsonBody, GetTasksResponse::class.java)

            if (data != null && data.notes.isNotEmpty()) {
                onTasksReceived(data.notes.filter { !it.isCompleted }, null)
            } else {
                onTasksReceived(emptyList(), NullPointerException("No data available!"))
            }
        }

        override fun onFailure(call: Call<ResponseBody?>, error: Throwable) {
            onTasksReceived(emptyList(), error)
        }
    })
  }

  fun deleteTask(onTaskDeleted: (Throwable?) -> Unit) {
    onTaskDeleted(null)
  }

  fun completeTask(onTaskCompleted: (Throwable?) -> Unit) {
    onTaskCompleted(null)
  }

  fun addTask(addTaskRequest: AddTaskRequest, onTaskCreated: (Task?, Throwable?) -> Unit) {
    onTaskCreated(
        Task("id3",
            addTaskRequest.title,
            addTaskRequest.content,
            false,
            addTaskRequest.taskPriority
        ), null
    )
  }

  fun getUserProfile(onUserProfileReceived: (UserProfile?, Throwable?) -> Unit) {
    onUserProfileReceived(UserProfile("mail@mail.com", "Filip", 10), null)
  }
}