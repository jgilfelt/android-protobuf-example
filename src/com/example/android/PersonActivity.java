package com.example.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.protobuf.R;
import com.example.tutorial.AddressBookProtos.Person;
import com.google.protobuf.InvalidProtocolBufferException;

public class PersonActivity extends Activity {

	public static final String PERSON_MSG_EXTRA = "person_msg_extra";
	public static final String PERSON_INDEX_EXTRA = "person_index_extra";
	public static final int NEW_PERSON_INDEX = -1;

	private Person.Builder person;
	private int personIndex = NEW_PERSON_INDEX;

	EditText editName;
	EditText editEmail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.person);

		// setup view references
		editName = (EditText) findViewById(R.id.edit_name);
		editEmail = (EditText) findViewById(R.id.edit_email);

		// read the serialized person from the intent extras
		Bundle b = getIntent().getExtras();
		if (b != null) {
			byte[] msg = b.getByteArray(PERSON_MSG_EXTRA);
			try {
				person = Person.parseFrom(msg).toBuilder();
				personIndex = b.getInt(PERSON_INDEX_EXTRA);
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
				person = Person.newBuilder();
			}
		} else {
			person = Person.newBuilder();
		}

		// set defaults if we are editing an existing person 
		if (person.isInitialized()) {
			editName.setText(person.getName());
			editEmail.setText(person.getEmail());
		}

	}

	public void onSaveClick(View v) {

		if (personIndex == NEW_PERSON_INDEX) {
			// we need a unique id for a new person object, obviously this isn't
			person.setId((int) System.currentTimeMillis());
		}

		// update and build the person object
		person.setName(editName.getText().toString());
		person.setEmail(editEmail.getText().toString());

		Person p = person.build();

		// serialize and pass back the person object to the calling activity
		Intent intent = new Intent();
		Bundle b = new Bundle();
		b.putByteArray(PERSON_MSG_EXTRA, p.toByteArray());
		b.putInt(PERSON_INDEX_EXTRA, personIndex);
		intent.putExtras(b);
		setResult(RESULT_OK, intent);
		finish();

	}

}
