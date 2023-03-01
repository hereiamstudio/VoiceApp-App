package com.voiceapp.data.model.simple

import androidx.annotation.Keep

/*
This class is used to hold reference to which simple data that is needed to reflect some UI changes,
such as if a interview has been seen, and the number of responses for any interview.

[{
	"id": "aaaa",
	"active": true,
	"interviews": [{
		"id": "bbbb",
		"seen": true,
		"responses": ['cccc']
	}]
}]


interviews

{
  "id"
  "project_id"
  "responses"
  "seen"
}

 */

@Keep
data class InterviewInfo(val id:String, val project_id:String, var seen:Boolean, val responses:MutableList<String>){
    fun getResponseCount(): Int {
        return responses.size
    }
}
