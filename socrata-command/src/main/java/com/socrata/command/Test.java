package com.socrata.command;

import au.com.bytecode.opencsv.CSVReader;
import com.socrata.api.Connection;
import com.socrata.api.HttpConnection;
import com.socrata.api.RequestException;
import com.socrata.data.View;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Username: ");
        String username = in.readLine();

        System.out.print("Password: ");
        String password = in.readLine();

        Connection c = new HttpConnection("opendata.socrata.com", username, password, "CGxaHQoQlgQSev4zyUh5aR5J3");

        try {
            // Pick out our view
            View v = View.find("nv7g-i6b6", c);
            System.out.println("Dataset " + v.getId() + ": " + v.getName());
            for(View.Column col : v.getColumns()) {
                System.out.println("\tColumn: " + col.getFieldName() + ", " + col.getId());
            }

            // Mark it for publishing
            View draftCopy = v.copy(c);
            System.out.println("Draft Copy " + draftCopy.getId() + ": " + draftCopy.getName());
            for(View.Column col : draftCopy.getColumns()) {
                System.out.println("\tColumn: " + col.getFieldName() + ", " + col.getId());
            }

            // Read an update CSV
            CSVReader reader = new CSVReader(new FileReader("update.csv"));
            String header[] = reader.readNext();
            String line[] = null;

            List<View.NewRow> records = new ArrayList<View.NewRow>();
            while((line = reader.readNext()) != null) {
                View.NewRow row = new View.NewRow();

                for(int i = 0; i < line.length; i++) {
                    View.Column col = draftCopy.getColumnByApiIdentifier(header[i]);
                    if(col == null) {
                        System.out.println("Could not find column matching identifier: " + header[i]);
                        continue;
                    }

                    row.putDataField(col, line[i]);
                }

                System.out.println(row.getDataFieldsForSerialization().toString());
                records.add(row);
            }

            // Push records using the upsert API
            View.BulkResults result = draftCopychfris.upsert(records, c);
            System.out.println("Updates: " + result.getRowsUpdated());
            System.out.println("Creates: " + result.getRowsCreated());
            System.out.println("Deleted: " + result.getRowsDeleted());
            System.out.println("Errors: " + result.getErrors());

            // Publish our updated version
            v = draftCopy.publish(c);

        } catch (RequestException e) {
            System.out.println("Error while updating dataset:");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error reading CSV file:");
            e.printStackTrace();
        }


    }
}
