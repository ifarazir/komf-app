/*
 * Copyright (C) 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.mvp2.network

import com.example.mvp2.database.DatabaseVocab
import com.example.mvp2.domain.*
import com.example.mvp2.lesson.LessonStatus
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkCoursesContainer(val data: List<NetworkCourse>)

@JsonClass(generateAdapter = true)
data class NetworkMyCoursesContainer(val data: List<NetworkMyCourse>)

@JsonClass(generateAdapter = true)
data class NetworkQuizContainer(val data:List<NetworkQuestion>)

@JsonClass(generateAdapter = true)
data class NetworkVocabContainer(val data: List<NetworkVocab>)


@JsonClass(generateAdapter = true)
data class NetworkLessonContainer(val data: List<NetworkLesson>)


@JsonClass(generateAdapter = true)
data class NetworkQuiz(
        //val time:Int,
        val data:List<NetworkQuestion> = ArrayList()
)


@JsonClass(generateAdapter = true)
data class NetworkQuestion(
        @Json(name="title")
        val questionText:String,
        @Json(name="q1")
        val choiceOne:String,
        @Json(name="q2")
        val choiceTwo:String,
        @Json(name="q3")
        val choiceThree:String,
        @Json(name="q4")
        val choiceFour:String,
        @Json(name="answer")
        val correctChoice:Int
)


@JsonClass(generateAdapter = true)
data class NetworkVocab(
    @Json(name="id")
    val vocabId:Long,
    val word:String,
    val syn:String,
    val def:String,
    val ex1:String,
    val ex2:String
)

@JsonClass(generateAdapter = true)
data class NetworkMyCourse(
        @Json(name="id")
        val courseId : Long,
        @Json(name="title")
        val courseTitle : String,
        @Json(name="description")
        val courseDescription : String,
        @Json(name="price")
        val coursePrice : Float,
        @Json(name="photo_url")
        val coursePhoto : String
)


@JsonClass(generateAdapter = true)
data class NetworkCourse(
        @Json(name="id")
        val courseId : Long,
        @Json(name="title")
        val courseTitle : String,
        @Json(name="description")
        val courseDescription : String,
        @Json(name="price")
        val coursePrice : Float,
        @Json(name="lessons")
        val courseLessons : List<NetworkLesson2>,
        @Json(name="photo_url")
        val photoUrl : String
//        @Json(name="status")
//        val availabilityStatus : String
)


@JsonClass(generateAdapter = true)
data class NetworkLesson(
    @Json(name="id")
    val lessonId:Long,
    @Json(name="title")
    val lessonTitle:String,
    @Json(name="prgress")
    val progress:Int
//    @Json(name="total_words")
//    val totalWords:String,
//    @Json(name="word_ids")
//    val wordIds:List<Long>
)


@JsonClass(generateAdapter = true)
data class NetworkLesson2(
        @Json(name="id")
        val lessonId:Long,
        @Json(name="title")
        val lessonTitle:String
)


fun NetworkQuizContainer.asDomainModel(): Quiz {

    val questionsList : List<Question> = data.map {
        Question(
                questionText = it.questionText,
                choiceOne = it.choiceOne,
                choiceTwo = it.choiceTwo,
                choiceThree = it.choiceThree,
                choiceFour = it.choiceFour,
                correctChoice = it.correctChoice)
    }

    val questions = ArrayList<Question>()
    questions.addAll(questionsList)

    return Quiz(
            time = 3,//quiz.time, //Todo: fix this later
            questions =  questions
    )
}




fun NetworkVocabContainer.asDomainModel(): List<Vocab> {
    return data.map {
        Vocab(
                vocabId = it.vocabId,
                lessonId = 0,//Todo: Remove this attribute later
                word = it.word,
                syn = it.syn,
                def = it.def,
                ex1 = it.ex1,
                ex2 = it.ex2)
    }
}


fun NetworkLessonContainer.asDomainModel(): List<Lesson> {
    return data.map {

        var status:LessonStatus = LessonStatus.ACTIVE

        if(it.progress == 100){
            status = LessonStatus.COMPLETED
        }
//        else if (it.progress == 0){
//            status = LessonStatus.INACTIVE
//        }

        Lesson(
            lessonId = it.lessonId,
            lessonTitle = it.lessonTitle,
            lessonProgress = it.progress,
            lessonStatus = status,
            remainedVocabIds = ArrayList() //Todo: remove this later when all the dependencies are fixed!
        )
    }
}


fun NetworkMyCoursesContainer.asDomainModel(): List<Course> {
    return data.map {

//        var status:CourseStatus = CourseStatus.Open
//
//        if(it.availabilityStatus == "restricted"){
//            status = CourseStatus.Restricted
//        }
//        else if (it.availabilityStatus == "not_ready"){
//            status = CourseStatus.NotReady
//        }

        Course(
                it.courseId,
                it.courseTitle,
                it.courseDescription,
                it.coursePhoto,
                it.coursePrice,
                null
        )
    }
}


fun NetworkCoursesContainer.asDomainModel(): List<Course> {
    return data.map {


        Course(
                it.courseId,
                it.courseTitle,
                it.courseDescription,
                it.photoUrl,
                it.coursePrice,
                it.courseLessons.map { lesson->
                    GeneralLesson(lesson.lessonId,lesson.lessonTitle)
                }
        )
    }
}


