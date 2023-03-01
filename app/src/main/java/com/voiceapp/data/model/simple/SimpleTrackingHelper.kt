package com.voiceapp.data.model.simple

import android.app.Activity
import android.content.SharedPreferences
import com.voiceapp.voiceappApplication
import com.voiceapp.data.model.Project
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber

object SimpleTrackingHelper {

    private const val SHARED_PREFS = "tracker"
    private const val FIELD_INTERVIEWS = "interviews"

    private var interviewsInfos: MutableList<InterviewInfo> = mutableListOf()
    private val gson = Gson()
    private var updating = false

    init {
        load()

        // TODO: this is temporary during the refactor. This will be done properly afterwards.
        FirebaseAuth.getInstance().addAuthStateListener {
            if (it.currentUser == null) {
                clearAll()
            }
        }
    }

    fun updateProjects(projects: List<Project>) {

//        if (updating){
//            Timber.e("Already updating")
//            return
//        }

        updating = true

        // Remove deleted projects from list...

        Timber.d("Interview infos: %d", interviewsInfos.size)

        // This works
        with(interviewsInfos.iterator()){
            forEach {
                interviewsInfo ->
                if(interviewsInfo.project_id  !in projects.map { it.id }){
                    Timber.d("Removing %s from interview infos as project is gone", interviewsInfo.project_id)
                    remove()
                }
            }
        }

        Timber.d("Interview infos: %d", interviewsInfos.size)

        for (project in projects) {
            Timber.d("updateProjects project %s", project.id)
            for (interview in project.interviews) {

                Timber.d("updateProjects interview %s", interview.id)

                val interviewInfo = getInterviewInfo(interview.id!!)

                if (interviewInfo == null) {
                    // New interview
                    interviewsInfos.add(
                        InterviewInfo(
                            interview.id!!,
                            project.id!!,
                            false,
                            mutableListOf()
                        )
                    )
                }
            }
        }
        save()

        updating = false
    }

    fun updateSeen(interviewId: String) {
        val interviewInfo = getInterviewInfo(interviewId)
        if (interviewInfo == null) {
            Timber.d("Interview missing from list...")
        } else {
            interviewInfo.seen = true
        }
        save()
    }

    fun getSeen(interviewId: String): Boolean {
        val interviewInfo = getInterviewInfo(interviewId)
        return if (interviewInfo == null) {
            Timber.w("Interview missing from list...")
            false
        } else {
            interviewInfo.seen
        }
    }

    fun updateResponses(interviewId: String, responseId: String) {
        val interviewInfo = getInterviewInfo(interviewId)
        if (interviewInfo == null) {
            Timber.w("Interview missing from list...")
        } else {
            interviewInfo.responses.add(responseId)
        }
        save()
    }

    fun getResponseCount(interviewId: String): Int {
        val interviewInfo = getInterviewInfo(interviewId)
        return if (interviewInfo == null) {
            Timber.w("Interview missing from list...")
            0
        } else {
            interviewInfo.getResponseCount()
        }
    }

    private fun getInterviewInfo(id: String): InterviewInfo? {
        // Get project info if it already exists
        for (interviewInfo in interviewsInfos.filter { it.id == id }) {
            return interviewInfo
        }
        return null
    }

    private fun save() {
        val json = gson.toJson(interviewsInfos)
        Timber.d("Json is %s", json)
        getSharedPrefs().edit().putString(FIELD_INTERVIEWS, json).apply()
    }

    private fun load() {
        val interviewsJson = getSharedPrefs().getString(FIELD_INTERVIEWS, null)

        if (interviewsJson != null) {
            val itemType = object : TypeToken<MutableList<InterviewInfo>>() {}.type
            interviewsInfos = gson.fromJson<MutableList<InterviewInfo>>(interviewsJson, itemType)
        }

        Timber.d("Loaded json to java: %s", interviewsInfos.toString())
    }

    private fun getSharedPrefs(): SharedPreferences {
        return voiceappApplication.instance.getSharedPreferences(SHARED_PREFS, Activity.MODE_PRIVATE)
    }

    fun clearAll() {
        interviewsInfos.clear()
        save()
    }
}