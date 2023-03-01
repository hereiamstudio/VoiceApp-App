package com.voiceapp.data.model.upload

import androidx.annotation.Keep
import com.voiceapp.data.model.BaseObject
import java.util.*

/*
project_id
interview_id
enumerator_id
start_time
end_time
age
gender
is_beneficiary
    questions [
    id
    title
    order
    is_custom
    is_flagged
    is_skipped
    [answers]
    ]
 */

@Keep
class Response(
    var project_id: String,
    var interview_id: String,
    var enumerator_id: String,
    var age: Int?,
    var gender: String?,
    var beneficiary: Boolean?,
    var consent_relationship: String?,
    var start_time: Date?,
    var end_time: Date?,
    val questions: MutableList<ResponseQuestion>,
    val enumerator_notes: String?
) : BaseObject(){

    override fun toString(): String {
        // Make it json style for easier reading
        return "{" +
                "\"id\":\"$id\"," +
                "\"project_id\":\"$project_id\"," +
                "\"interview_id\":\"$interview_id\"," +
                "\"enumerator_id\":\"$enumerator_id\"," +

                "\"gender\":\"$gender\"," +
                "\"consent_relationship\":\"$consent_relationship\"," +
                "\"age\":$age," +
                "\"beneficiary\":$beneficiary," +
                "\"start_time\":\"$start_time\"," +
                "\"end_time\":\"$end_time\"," +
                "\"questions\":$questions," +
                "\"enumerator_notes\":$enumerator_notes," +
                "}"
    }
}