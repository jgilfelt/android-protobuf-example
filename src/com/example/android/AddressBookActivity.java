package com.example.android;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.example.tutorial.AddressBookProtos.AddressBook;
import com.example.tutorial.AddressBookProtos.Person;

public class AddressBookActivity extends ListActivity {
   
	private static final String ADDRESS_BOOK_FILE = "address_book.file";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
                
        AddressBook ab = readAddressBook();
        
        //TextView tv = (TextView) findViewById(R.id.text);
        //tv.setText(ab.toString());
        
        ListAdapter adapter = new AddressBookAdapter(this, ab.getPersonList());
        setListAdapter(adapter);
        
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
    
    private AddressBook readAddressBook() {
    	try {
			return AddressBook.parseFrom(openFileInput(ADDRESS_BOOK_FILE));
		} catch (FileNotFoundException e) {
			try {
				copyFromAssets();
				return AddressBook.parseFrom(openFileInput(ADDRESS_BOOK_FILE));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return AddressBook.getDefaultInstance();
    }
    
    
}