package com.example.android;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.protobuf.R;
import com.example.tutorial.AddressBookProtos.AddressBook;
import com.example.tutorial.AddressBookProtos.Person;
import com.google.protobuf.InvalidProtocolBufferException;

public class AddressBookActivity extends ListActivity implements OnItemClickListener {

	private static final String ADDRESS_BOOK_FILE = "address_book.file";

	static final private int PERSON_EDIT_CODE = 0;

	private AddressBook.Builder addressBook;
	private AddressBookAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// read address book from local storage
		addressBook = readAddressBook();
		if (addressBook.getPersonCount() == 0) {
			// no data, so populate from the binary file included in assets
			addressBook.mergeFrom(readAddressBookFromAssets());
			writeAddressBook();
		}

		// set our list adapter
		adapter = new AddressBookAdapter(this, addressBook.getPersonList());
		setListAdapter(adapter);
		getListView().setOnItemClickListener(this);

	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		// get selected person
		Person p = addressBook.getPerson(position);

		// serialize the person as a byte array and pass to our person activity
		Intent intent = new Intent(this, PersonActivity.class);
		Bundle b = new Bundle();
		b.putByteArray(PersonActivity.PERSON_MSG_EXTRA, p.toByteArray());
		b.putInt(PersonActivity.PERSON_INDEX_EXTRA, position);
		intent.putExtras(b);
		startActivityForResult(intent, PERSON_EDIT_CODE);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == PERSON_EDIT_CODE) {
			if (resultCode != RESULT_CANCELED) {

				// read the serialized person object from the data
				Bundle b = data.getExtras();
				byte[] msg = b.getByteArray(PersonActivity.PERSON_MSG_EXTRA);
				int index = b.getInt(PersonActivity.PERSON_INDEX_EXTRA);

				Person p;
				try {
					p = Person.parseFrom(msg);

					if (index != PersonActivity.NEW_PERSON_INDEX) {
						// update address book
						addressBook.setPerson(index, p);
					} else {
						// add to address book
						addressBook.addPerson(p);
					}

					// recreate adapter with new data - a bit heavyweight but works for this example
					adapter = new AddressBookAdapter(this, addressBook.getPersonList());
					setListAdapter(adapter);

					// write the address book to local storage
					writeAddressBook();

				} catch (InvalidProtocolBufferException e) {
					e.printStackTrace();
				}

			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add:
			// start our activity with no extra data to create a new person
			Intent intent = new Intent(this, PersonActivity.class);
			startActivityForResult(intent, PERSON_EDIT_CODE);
			break;
		}
		return true;
	}

	private static class AddressBookAdapter extends ArrayAdapter<Person> {

		private LayoutInflater mInflater;
		private List<Person> mObjects;

		public AddressBookAdapter(Context context, List<Person> objects) {
			super(context, 0, objects);
			mInflater = LayoutInflater.from(context);
			mObjects = objects;
		}

		@Override
		public long getItemId(int position) {
			return mObjects.get(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// standard view holder pattern boilerplate
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(android.R.layout.simple_list_item_2, null);
				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(android.R.id.text1);
				holder.email = (TextView) convertView.findViewById(android.R.id.text2);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// map person fields to corresponding textviews
			Person person = mObjects.get(position);
			holder.name.setText(person.getName());
			holder.email.setText(person.getEmail());

			return convertView;
		}

		static class ViewHolder {
			TextView name;
			TextView email;
		}

	}


	private AddressBook.Builder readAddressBook() {

		try {
			return AddressBook.parseFrom(openFileInput(ADDRESS_BOOK_FILE)).toBuilder();
		} catch (FileNotFoundException e) {
			Log.i(getString(R.string.app_name), "No address book file previously saved");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return AddressBook.newBuilder();

	}

	private void writeAddressBook() {

		try {
			addressBook.build().writeTo(openFileOutput(ADDRESS_BOOK_FILE, Context.MODE_PRIVATE));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private AddressBook readAddressBookFromAssets() {

		InputStream is;
		try {
			is = getAssets().open(ADDRESS_BOOK_FILE);
			return AddressBook.parseFrom(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return AddressBook.getDefaultInstance();

	}

}