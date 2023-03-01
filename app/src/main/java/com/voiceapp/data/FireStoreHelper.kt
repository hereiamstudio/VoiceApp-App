package com.voiceapp.data

import com.voiceapp.data.model.simple.SimpleTrackingHelper
import com.voiceapp.data.model.upload.Response
import com.voiceapp.data.model.upload.ResponseEncrypter
import com.voiceapp.data.model.upload.ResponseQuestion
import com.voiceapp.data.model.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList
import kotlin.coroutines.resume

@Singleton
class FireStoreHelper @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val responseEncrypter: ResponseEncrypter
) {

    companion object {

        // Groups collections
        const val GROUP_PROJECTS: String = "projects_public"
        const val GROUP_INTERVIEWS: String = "interviews_public"
        const val GROUP_QUESTIONS: String = "questions_public"
        const val GROUP_TEMPLATES: String = "templates_public"

        // Fields
        const val FIELD_ASSIGNED_USER_IDS = "assigned_users_ids"
        const val FIELD_ARCHIVED = "is_archived"
        const val FIELD_ACTIVE = "is_active"
        const val FIELD_CREATED = "created_at"
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    private fun getUid(): String? {
        return auth.currentUser?.uid
    }

    private fun <T : BaseObject> onResult(
        document: DocumentSnapshot?,
        e: FirebaseFirestoreException?,
        onSuccess: OnSuccessListener<T>,
        onFailListener: OnFailureListener,
        clazz: T
    ) {

        if (e != null) {
            onFailListener.onFailure(e)
            return
        }

        if (document == null) {
            onSuccess.onSuccess(null)
            return
        }

        Timber.d("${document.id} => ${document.data}")
        val obj = document.toObject(clazz::class.java)
        obj?.id = document.id
        onSuccess.onSuccess(obj)
    }

    private fun <T : BaseObject> onResults(
        documents: QuerySnapshot?,
        e: FirebaseFirestoreException?,
        onSuccess: OnSuccessListener<List<T>>,
        onFailListener: OnFailureListener,
        clazz: T
    ) {

        if (e != null) {
            Timber.d("fail results")
            onFailListener.onFailure(e)
            return
        }

        val list: MutableList<T> = ArrayList()

        if (documents == null) {
            Timber.d("null results")
            onSuccess.onSuccess(list)
            return
        }

        if (documents.size() == 0) {
            Timber.d("empty results")
            onSuccess.onSuccess(list)
            return
        }

        for (document in documents) {
            Timber.d("${document.id} => ${document.data}")
            val obj = document.toObject(clazz::class.java)
            obj.id = document.id
            list.add(obj)
        }

        onSuccess.onSuccess(list)
    }


    fun getProject(
        projectId: String,
        onSuccess: OnSuccessListener<Project>,
        onFailListener: OnFailureListener,
        live: Boolean = false
    ) {

        val dr = db.collection("/projects/$projectId/projects_public/").document(projectId)

        if (live) {
            // Perform live request
            dr.get()
                .addOnSuccessListener { document ->
                    onResult(
                        document,
                        null,
                        onSuccess,
                        onFailListener,
                        Project()
                    )
                }
                .addOnFailureListener(onFailListener)
        } else {
            // Perform cached request
            dr.addSnapshotListener { document, e ->
                onResult(
                    document,
                    e,
                    onSuccess,
                    onFailListener,
                    Project()
                )
            }
        }
    }

    fun getProjects(
        onSuccess: OnSuccessListener<List<Project>>,
        onFailListener: OnFailureListener,
        live: Boolean = false
    ) {

        val uid = getUid() ?: return

        val dr = db.collectionGroup(GROUP_PROJECTS)
        val query = dr
            .whereEqualTo(FIELD_ARCHIVED, false)
            .whereEqualTo(FIELD_ACTIVE, true)
            .whereArrayContains(FIELD_ASSIGNED_USER_IDS, uid)
            .orderBy(FIELD_CREATED, Query.Direction.DESCENDING)

        if (live) {
            // Perform live request
            query.get()
                .addOnSuccessListener { documents ->
                    onResults(
                        documents,
                        null,
                        onSuccess,
                        onFailListener,
                        Project()
                    )
                }
                .addOnFailureListener { exception -> onFailListener.onFailure(exception) }
        } else {
            // Perform cached request
            query.addSnapshotListener { documents, e ->
                onResults(
                    documents,
                    e,
                    onSuccess,
                    onFailListener,
                    Project()
                )
            }
        }
    }

    private fun getProjectsAsync(
        live: Boolean = false,
        callback: (List<Project>) -> Unit
    ) {
        getProjects(
            OnSuccessListener(callback),
            OnFailureListener { exception -> Timber.e(exception) },
            live = live
        )
    }

    suspend fun getProjectsSync(live: Boolean = false): List<Project> =
        suspendCancellableCoroutine { cont ->
            getProjectsAsync(live = live) {
                if (cont.isActive) {
                    cont.resume(it)
                }
            }
        }

    fun getInterviews(
        projectId: String?,
        onSuccess: OnSuccessListener<List<Interview>>,
        onFailListener: OnFailureListener,
        live: Boolean = false
    ) {

        if (projectId == null) return
        val uid = getUid() ?: return

        val dr = db.collectionGroup(GROUP_INTERVIEWS)

        val query = dr
            .whereArrayContains(FIELD_ASSIGNED_USER_IDS, uid)
            .whereEqualTo("status", "active")
            .whereEqualTo("project.id", projectId)
            .whereEqualTo(FIELD_ARCHIVED, false)
            .orderBy(FIELD_CREATED, Query.Direction.DESCENDING)

        if (live) {
            // Perform live request
            query.get()
                .addOnSuccessListener { documents ->
                    onResults(
                        documents,
                        null,
                        onSuccess,
                        onFailListener,
                        Interview()
                    )
                }
                .addOnFailureListener { exception -> onFailListener.onFailure(exception) }
        } else {
            // Perform cached request
            query.addSnapshotListener { documents, e ->
                onResults(
                    documents,
                    e,
                    onSuccess,
                    onFailListener,
                    Interview()
                )
            }
        }
    }

    private fun getInterviewsAsync(
        projectId: String,
        live: Boolean = false,
        callback: (List<Interview>) -> Unit
    ) {
        getInterviews(
            projectId,
            OnSuccessListener(callback),
            OnFailureListener { exception -> Timber.e(exception) },
            live = live
        )
    }

    suspend fun getInterviewsSync(projectId: String, live: Boolean = false): List<Interview> =
        suspendCancellableCoroutine { cont ->
            getInterviewsAsync(projectId, live = live) {
                if (cont.isActive) {
                    cont.resume(it)
                }
            }
        }


    fun getQuestions(
        projectId: String?,
        interviewId: String?,
        onSuccess: OnSuccessListener<List<Question>>,
        onFailListener: OnFailureListener,
        live: Boolean = false
    ) {

        if (projectId == null || interviewId == null) return

        val dr = db.collectionGroup(GROUP_QUESTIONS)
        val query = dr
            .whereEqualTo("interview.id", interviewId)
            .whereEqualTo(FIELD_ARCHIVED, false)
            .orderBy("order", Query.Direction.ASCENDING)

        if (live) {
            // Perform live request
            query.get()
                .addOnSuccessListener { documents ->
                    onResults(
                        documents,
                        null,
                        onSuccess,
                        onFailListener,
                        Question()
                    )
                }
                .addOnFailureListener { exception -> onFailListener.onFailure(exception) }
        } else {
            // Perform cached request
            query.addSnapshotListener { documents, e ->
                onResults(
                    documents,
                    e,
                    onSuccess,
                    onFailListener,
                    Question()
                )
            }
        }
    }


    fun getQuestion(
        projectId: String?,
        interviewId: String?,
        questionId: String?,
        onSuccess: OnSuccessListener<Question>,
        onFailListener: OnFailureListener,
        live: Boolean = false
    ) {

        if (projectId == null || interviewId == null || questionId == null) return

        val dr = db.collection("projects/$projectId/interviews/$interviewId/questions_public")
            .document(questionId)

        if (live) {
            // Perform live request
            dr.get()
                .addOnSuccessListener { documents ->
                    onResult(
                        documents,
                        null,
                        onSuccess,
                        onFailListener,
                        Question()
                    )
                }
                .addOnFailureListener { exception ->
                    onFailListener.onFailure(exception)
                }
        } else {
            // Perform cached request
            dr.addSnapshotListener { documents, e ->
                onResult(
                    documents,
                    e,
                    onSuccess,
                    onFailListener,
                    Question()
                )
            }
        }
    }

    private fun getQuestionsAsync(
        projectId: String,
        interviewId: String,
        live: Boolean = false,
        callback: (List<Question>) -> Unit
    ) {
        getQuestions(
            projectId,
            interviewId,
            OnSuccessListener(callback),
            OnFailureListener { exception -> Timber.e(exception) },
            live = live
        )
    }

    suspend fun getQuestionsSync(
        projectId: String,
        interviewId: String,
        live: Boolean = false
    ): List<Question> =
        suspendCancellableCoroutine { cont ->
            getQuestionsAsync(projectId, interviewId, live = live) {
                if (cont.isActive) {
                    cont.resume(it)
                }
            }
        }


    fun getTemplates(
        onSuccess: OnSuccessListener<List<Template>>,
        onFailListener: OnFailureListener,
        live: Boolean = false
    ) {

        val dr = db.collectionGroup(GROUP_TEMPLATES)
        val query = dr
            .whereEqualTo(FIELD_ARCHIVED, false)
            .orderBy("question_title")

        if (live) {
            // Perform live request
            query.get()
                .addOnSuccessListener { documents ->
                    onResults(
                        documents,
                        null,
                        onSuccess,
                        onFailListener,
                        Template()
                    )
                }
                .addOnFailureListener { exception -> onFailListener.onFailure(exception) }
        } else {
            // Perform cached request
            query.addSnapshotListener { documents, e ->
                onResults(
                    documents,
                    e,
                    onSuccess,
                    onFailListener,
                    Template()
                )
            }
        }
    }

    private fun getTemplatesAsync(live: Boolean = false, callback: (List<Template>) -> Unit) {
        getTemplates(
            OnSuccessListener(callback),
            OnFailureListener { exception -> Timber.e(exception) },
            live = live
        )
    }

    suspend fun getTemplatesSync(live: Boolean = false): List<Template> =
        suspendCancellableCoroutine { cont ->
            getTemplatesAsync(live = live) {
                if (cont.isActive) {
                    cont.resume(it)
                }
            }
        }

    fun createUploadResponse(
        startTime: Date,
        projectId: String,
        interviewId: String,
        respondentInfo: RespondentInfo,
        questions: MutableList<Question>,
        answers: HashMap<String, Answer>,
        enumeratorNotes: String?
    ): Response {

        val responseQuestions = mutableListOf<ResponseQuestion>()

        for (question in questions.sortedBy { it.order }) {

            val answer = answers[question.id]

            val skipped = answer?.skipped ?: true
            val flagged = answer?.flagged ?: false
            val starred = answer?.starred ?: false

            val rq = ResponseQuestion(
                question.id,
                question.title,
                question.order,
                question.type,
                question.is_probing_question,
                skipped,
                flagged,
                starred,
                answer?.usedMicrophone ?: false,
                answer?.answerOptions,
                answer?.transcribedAnswer)

            responseQuestions.add(rq)
        }

        val response = Response(
            projectId,
            interviewId,
            getUid()!!,
            respondentInfo.age,
            respondentInfo.gender!!,
            respondentInfo.benificiary!!,
            respondentInfo.consent_relationship,
            startTime,
            Calendar.getInstance().time,
            responseQuestions,
            enumeratorNotes)

        Timber.w("Encrypted response: %s", response.toString())

        return response
    }

    fun saveResponse(
        response: Response,
        onSuccessListener: OnSuccessListener<Any>,
        onFailureListener: OnFailureListener
    ) {

        val id = UUID.randomUUID().toString()
        response.id = id

        Timber.d("Uploading proj ${response.project_id} int ${response.interview_id} $id")
        Timber.d("Path projects/${response.project_id}/interviews/${response.interview_id}/responses_queue")
        Timber.d("Response $response")

        db.collection("projects/${response.project_id}/interviews/${response.interview_id}/responses_queue")
            .document(id)
            .set(responseEncrypter.encryptResponse(response))
            .addOnSuccessListener { onSuccessListener.onSuccess(null) }
            .addOnFailureListener { e -> onFailureListener.onFailure(e) }

        SimpleTrackingHelper.updateResponses(response.interview_id, id)
    }

    fun allResponsesUploaded(onSuccess: OnSuccessListener<Boolean>) {
        Timber.d("Checking if responses are uploaded...")
        db.waitForPendingWrites().addOnCompleteListener {
            Timber.d("All Responses are Uploaded")
            onSuccess.onSuccess(true)
        }
    }

    private fun isAnyResponseNotUploadedYet(
        projectId: String,
        interviewId: String,
        onSuccess: OnSuccessListener<Boolean>
    ) {

        db.collection("projects/${projectId}/interviews/${interviewId}/responses_queue")
            .addSnapshotListener { documents, e ->

                if (e != null) {
                    Timber.e(e)
                    onSuccess.onSuccess(false)
                    return@addSnapshotListener
                }

                if (documents == null) {
                    Timber.e("Responses is null...")
                    onSuccess.onSuccess(false)
                    return@addSnapshotListener
                }

                Timber.d("Documents ${documents.size()}")

                for (document in documents) {
                    if (document.metadata.hasPendingWrites()) {
                        onSuccess.onSuccess(true)
                        return@addSnapshotListener
                    }
                }

                onSuccess.onSuccess(false)
            }
    }


    fun getLanguages(){

    }

}