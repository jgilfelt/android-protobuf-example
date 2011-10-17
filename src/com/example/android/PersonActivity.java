package com.example.android;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.protobuf.R;
import com.example.tutorial.AddressBookProtos.Person;
import com.example.tutorial.AddressBookProtos.Person.PhoneNumber;
import com.example.tutorial.AddressBookProtos.Person.PhoneType;
import com.google.protobuf.InvalidProtocolBufferException;

public class PersonActivity extends Activity {

	public static final String PERSON_MSG_EXTRA = "person_msg_extra";
	public static final String PERSON_INDEX_EXTRA = "person_index_extra";
	public static final int NEW_PERSON_INDEX = -1;

	private Person.Builder person;
	private int personIndex = NEW_PERSON_INDEX;

	EditText editName;
	EditText editEmail;
	ListView phones;
	
	ListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.person);

		// setup view references
		editName = (EditText) findViewById(R.id.edit_name);
		editEmail = (EditText) findViewById(R.id.edit_email);
		phones = (ListView) findViewById(R.id.list_phone);

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
			
			adapter = new PhoneNumberAdapter(this, person.getPhoneList());
			phones.setAdapter(adapter);
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
	
	public void onAddClick(View v) {

		// launch phone number entry dialog
		LayoutInflater factory = LayoutInflater.from(this);
		final View layoutView = factory.inflate(R.layout.phone_number, null);

		final EditText number = (EditText) layoutView.findViewById(R.id.edit_number);
		final RadioGroup ptype = (RadioGroup) layoutView.findViewById(R.id.radioGroup_type);

		new AlertDialog.Builder(PersonActivity.this)
		.setTitle("Phone Number")
		.setView(layoutView)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				// create a new phonenumber
				PhoneNumber.Builder phoneNumber = PhoneNumber.newBuilder();
				phoneNumber.setNumber(number.getText().toString());
				PhoneType type = null;
				switch (ptype.getCheckedRadioButtonId()) {
				case R.id.radio0:
					type = PhoneType.MOBILE;
					break;
				case R.id.radio1:
					type = PhoneType.HOME;
					break;
				case R.id.radio2:
					type = PhoneType.WORK;
					break;
				}
				phoneNumber.setType(type);
				
				// add the new number to our person
				person.addPhone(phoneNumber.build());
				
				// reload adapter
				adapter = new PhoneNumberAdapter(PersonActivity.this, person.getPhoneList());
				phones.setAdapter(adapter);

			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {}
		})
		.create().show();

	}
	
	private static class PhoneNumberAdapter extends ArrayAdapter<PhoneNumber> {

		private LayoutInflater mInflater;
		private List<PhoneNumber> mObjects;

		public PhoneNumberAdapter(Context context, List<PhoneNumber> objects) {
			super(context, 0, objects);
			mInflater = LayoutInflater.from(context);
			mObjects = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// standard view holder pattern boilerplate
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(android.R.layout.simple_list_item_2, null);
				holder = new ViewHolder();
				holder.number = (TextView) convertView.findViewById(android.R.id.text1);
				holder.type = (TextView) convertView.findViewById(android.R.id.text2);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// map PhoneNumber fields to corresponding textviews
			PhoneNumber phone = mObjects.get(position);
			holder.number.setText(phone.getNumber());
			holder.type.setText(phone.getType().toString());

			return convertView;
		}

		static class ViewHolder {
			TextView number;
			TextView type;
		}

	}

}
