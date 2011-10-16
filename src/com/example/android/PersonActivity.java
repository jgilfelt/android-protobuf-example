package com.example.android;

import com.example.protobuf.R;
import com.example.tutorial.AddressBookProtos.Person;
import com.google.protobuf.InvalidProtocolBufferException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
		
		editName = (EditText) findViewById(R.id.edit_name);
		editEmail = (EditText) findViewById(R.id.edit_email);
		
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
		
		if (person.isInitialized()) {
			editName.setText(person.getName());
			editEmail.setText(person.getEmail());
		}
	
	}
	
	public void onSaveClick(View v) {
		
		if (personIndex == NEW_PERSON_INDEX) {
			person.setId((int) System.currentTimeMillis());
		}
		
		person.setName(editName.getText().toString());
		person.setEmail(editEmail.getText().toString());
		
		Person p = person.build();
		
		Intent intent = new Intent();
		Bundle b = new Bundle();
		b.putByteArray(PERSON_MSG_EXTRA, p.toByteArray());
		b.putInt(PERSON_INDEX_EXTRA, personIndex);
		intent.putExtras(b);
		setResult(RESULT_OK, intent);
		finish();
		
	}

}
