package com.example.android;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.example.protobuf.R;
import com.example.tutorial.AddressBookProtos.AddressBook;
import com.example.tutorial.AddressBookProtos.Person;
import com.google.protobuf.InvalidProtocolBufferException;

public class AddressBookActivity extends ListActivity implements OnItemClickListener {
   
	private static final String ADDRESS_BOOK_FILE = "address_book.file";
	
	static final private int PERSON_EDIT_CODE = 0;
	
	private AddressBook.Builder ab;
	private AddressBookAdapter adapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
        ab = readAddressBook().toBuilder();
        
        adapter = new AddressBookAdapter(this, ab.getPersonList());
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
       
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == PERSON_EDIT_CODE) {
			if (resultCode != RESULT_CANCELED) {

				Bundle b = data.getExtras();
				byte[] msg = b.getByteArray(PersonActivity.PERSON_MSG_EXTRA);
				int index = b.getInt(PersonActivity.PERSON_INDEX_EXTRA);

				Person p;
				try {
					p = Person.parseFrom(msg);

					if (index != PersonActivity.NEW_PERSON_INDEX) {
						ab.setPerson(index, p);
					} else {
						ab.addPerson(p);
					}

					adapter = new AddressBookAdapter(this, ab.getPersonList());
					setListAdapter(adapter);

					writeAddressBook();

				} catch (InvalidProtocolBufferException e) {
					e.printStackTrace();
				}

			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
    	Person p = ab.getPerson(position);
    	
		Intent intent = new Intent(this, PersonActivity.class);
		Bundle b = new Bundle();
		b.putByteArray(PersonActivity.PERSON_MSG_EXTRA, p.toByteArray());
		b.putInt(PersonActivity.PERSON_INDEX_EXTRA, position);
		intent.putExtras(b);
		startActivityForResult(intent, PERSON_EDIT_CODE);
		
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
			Intent intent = new Intent(this, PersonActivity.class);
			startActivityForResult(intent, PERSON_EDIT_CODE);
			break;
		}
		return true;
	}
    
    private static class AddressBookAdapter extends ArrayAdapter<Person> {
    	
    	private LayoutInflater mInflater;
        private Context mContext;
        private List<Person> mObjects;
    	
		public AddressBookAdapter(Context context, List<Person> objects) {
			super(context, 0, objects);
			mContext = context;
			mInflater = LayoutInflater.from(context);
			mObjects = objects;
		}
		
		@Override
		public long getItemId(int position) {
			return mObjects.get(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
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
    
    
    private AddressBook readAddressBook() {
    	
    	try {
			return AddressBook.parseFrom(openFileInput(ADDRESS_BOOK_FILE));
		} catch (FileNotFoundException e) {
			try {
				copyFromAssets();
				return AddressBook.parseFrom(openFileInput(ADDRESS_BOOK_FILE));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return AddressBook.getDefaultInstance();
		
    }
    
    private void writeAddressBook() {
    	
    	try {
			ab.build().writeTo(openFileOutput(ADDRESS_BOOK_FILE, Context.MODE_PRIVATE));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
    }
    
    private void copyFromAssets() throws IOException {
    	
    	InputStream is = getAssets().open(ADDRESS_BOOK_FILE);
        int size = is.available();
        
        // Read the entire asset into a local byte buffer.
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
    	
    	FileOutputStream fos = openFileOutput(ADDRESS_BOOK_FILE, Context.MODE_PRIVATE);
    	fos.write(buffer);
    	fos.close();
    	
    }
    
}