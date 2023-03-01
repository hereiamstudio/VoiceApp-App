package com.voiceapp.data.sync

import com.voiceapp.data.FireStoreHelper
import com.voiceapp.data.model.simple.SimpleTrackingHelper
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject
import kotlin.concurrent.thread

class SyncService @Inject constructor(
    private val fh: FireStoreHelper
) {

    private val simpleTrackingHelper = SimpleTrackingHelper
    private var syncing = false

    fun interface SyncListener {
        fun onSyncCompleted()
    }

    /**
     * This function syncs all the projects, interviews and questions so that it will be cached
     * by firestore and will be available for us to use offline.
     */
    fun syncEverything(syncListener: SyncListener? = null) {
        Timber.v("Starting syncing")

        if (syncing){
            Timber.w("Already syncing")
            return
        }

        syncing = true

        thread {

            runBlocking {

                val projects = fh.getProjectsSync(live = true)
                Timber.d("Got projects %d", projects.size)

                for (project in projects) {
                    Timber.d("Got project %s %s", project.id, project.title)

                    val interviews = fh.getInterviewsSync(project.id!!, live = true)

                    project.interviews = interviews

                    for (interview in interviews) {

                        Timber.d("Got interview %s %s", interview.id, interview.title)

                        val questions = fh.getQuestionsSync(project.id!!, interview.id!!, live = true)
                        Timber.d("Got questions %d", questions.size)
                    }
                }

                Timber.d("Updating tracking helper")
                simpleTrackingHelper.updateProjects(projects)

                val templates = fh.getTemplatesSync(live = true)
                Timber.d("Got templates %d", templates.size)
                for (template in templates){
                    Timber.d("template %s", template.question_title)
                }

            }

            Timber.d("syncEverything completed ")
            syncing = false
            syncListener?.onSyncCompleted()
        }
    }

}