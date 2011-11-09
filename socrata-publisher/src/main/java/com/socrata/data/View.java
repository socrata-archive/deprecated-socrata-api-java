package com.socrata.data;

import com.socrata.api.Connection;
import com.socrata.api.RequestException;
import com.socrata.api.Response;
import com.socrata.util.Strings;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.codehaus.jackson.annotate.*;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * "Views" are effectively data entities. They can be tabular data, file data,
 * or a link to an external datasource.
 *
 *
 * A View includes all of the metadata about a table or view, including
 * descriptive values such as name, description, and tags, as well as
 * structural data like the columns it contains and any sorts or filters.
 *
 * The View model allows you to create, read, update or delete socrata datasets
 * and filters. It also provides an interface for importing/appending/replacing
 * and publishing datasets.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class View extends Model<View>
{
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Column extends Model<Column>
    {
        private static final String[] RESERVED_FIELD_NAMES = { "_id", "_uuid", "_position", "_address" };

        int id;
        String name;
        String description;
        String dataTypeName;
        int position;
        Integer width;
        View view;

        public int getId()
        {
            return id;
        }

        private void setId(int id)
        {
            this.id = id;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        public String getDataTypeName()
        {
            return dataTypeName;
        }

        public void setDataTypeName(String dataTypeName)
        {
            this.dataTypeName = dataTypeName;
        }

        public int getPosition()
        {
            return position;
        }

        public void setPosition(int position)
        {
            this.position = position;
        }

        public Integer getWidth()
        {
            return width;
        }

        public void setWidth(int width)
        {
            this.width = width;
        }

        @JsonIgnore
        public View getView()
        {
            return view;
        }

        public void setView(View view)
        {
            this.view = view;
        }

        // TODO: This will need to be updated to reflect the API Identifier change when its deployed
        @JsonIgnore
        public String getFieldName()
        {
            if (name == null)
            {
                return null;
            }

            String fieldName = Strings.underscoreize(name);

            List<Column> columns = view.getColumns();
            for (Column column : columns)
            {
                if (this == column)
                {
                    continue;
                }

                if (Strings.underscoreize(column.getName()).equals(fieldName))
                {
                    fieldName += "_" + position;
                    return fieldName;
                }
            }

            for (String reservedName : RESERVED_FIELD_NAMES)
            {
                if (reservedName.equals(fieldName))
                {
                    fieldName += "_" + position;
                    return fieldName;
                }
            }

            return fieldName;
        }

        @Override
        String path()
        {
            return "/views/" + getView().getId() + "/columns";
        }

        @Override
        public Column create(Connection request) throws RequestException
        {
            return super.create(request, Column.class);
        }

        @Override
        public Column update(Connection request) throws RequestException
        {
            return super.update(Integer.toString(getId()), request, Column.class);
        }

        @Override
        public void delete(Connection request) throws RequestException
        {
            super.delete(Integer.toString(getId()), request);
        }

        @Override
        public Column clone()
        {
            Column clone = new Column();
            clone.setDataTypeName(getDataTypeName());
            clone.setId(getId());
            clone.setName(getName());
            clone.setPosition(getPosition());
            clone.setWidth(getWidth());

            return clone;
        }
    }

    public static class Row extends Model<Row>
    {
        int position;
        String id;
        int sid;
        Date updatedAt;
        Date createdAt;
        Map<Integer, Object> data;
        View view;

        public Row()
        {
            this.data = new HashMap<Integer, Object>();
        }

        @JsonIgnore
        public int getPosition()
        {
            return position;
        }

        private void setPosition(int position)
        {
            this.position = position;
        }

        @JsonIgnore
        public String getId()
        {
            return id;
        }

        private void setId(String id)
        {
            this.id = id;
        }

        public int getSid()
        {
            return sid;
        }

        private void setSid(int sid)
        {
            this.sid = sid;
        }

        @JsonIgnore
        public Date getUpdatedAt()
        {
            return updatedAt;
        }

        private void setUpdatedAt(Date updatedAt)
        {
            this.updatedAt = updatedAt;
        }

        @JsonIgnore
        public Date getCreatedAt()
        {
            return createdAt;
        }

        private void setCreatedAt(Date createdAt)
        {
            this.createdAt = createdAt;
        }

        @JsonIgnore
        public View getView()
        {
            return this.view;
        }

        protected void setView(View view)
        {
            this.view = view;
        }

        @JsonIgnore
        public Map<Column, Object> getAllData() {
            Map<Column, Object> result = new HashMap<Column, Object>();

            for (Map.Entry<Integer, Object> entry : this.data.entrySet())
            {
                result.put(this.view.getColumnById(entry.getKey()), entry.getValue());
            }

            return result;
        }

        @JsonAnySetter
        protected void putDataField(String key, Object value)
        {
            // here we grab unknown properties and dump them in to our
            // data hash

            Integer columnId;
            try
            {
                columnId = Integer.parseInt(key);
            }
            catch (NumberFormatException ex)
            {
                return; // we don't care about this field; it's not a column id
            }

            this.data.put(columnId, value);
        }

        public void putDataField(Column column, Object value)
        {
            if (column == null)
            {
                throw new IllegalArgumentException("Must supply a column to set data.");
            }

            this.data.put(column.id, value);
        }

        @JsonIgnore
        public Object getDataField(Column column)
        {
            return this.data.get(column.getId());
        }

        @JsonAnyGetter
        public Map<String, Object> getDataFieldsForSerialization()
        {
            Map<String, Object> result = new HashMap<String, Object>();
            for (Map.Entry<Integer, Object> entry : this.data.entrySet())
            {
                result.put(entry.getKey().toString(), entry.getValue());
            }

            return result;
        }

        @Override
        String path()
        {
            return "/views/" + getView().getId() + "/rows";
        }

        @Override
        public Row create(Connection request) throws RequestException
        {
            return super.create(request, Row.class);
        }

        @Override
        public Row update(Connection request) throws RequestException
        {
            return super.update(Integer.toString(getSid()), request, Row.class);
        }

        @Override
        public void delete(Connection request) throws RequestException
        {
            super.delete(Integer.toString(getSid()), request);
        }

        @Override
        public Row clone()
        {
            Row row = new Row();
            row.data = this.data; // TODO: ideally we should clone the data too... somehow
            return row;
        }
    }

    public static class NewRow extends Row
    {
        @Override
        @JsonAnyGetter
        public Map<String, Object> getDataFieldsForSerialization()
        {
            Map<String, Object> result = new HashMap<String, Object>();
            for (Map.Entry<Integer, Object> entry : this.data.entrySet())
            {
                result.put(":" + entry.getKey().toString(), entry.getValue());
            }

            return result;
        }
    }

    @JsonIgnoreProperties({ "address" })
    private static class SingleRow extends Row
    {
        @JsonProperty("_uuid")
        public void setId(String id)
        {
            super.setId(id);
        }

        @JsonProperty("_id")
        public void setSid(Integer sid)
        {
            super.setSid(sid);
        }

        @JsonProperty("_position")
        public void setPosition(Integer position)
        {
            super.setSid(position);
        }

        @JsonProperty("_address")
        public void setAddress(String address)
        {
            // don't care.
        }

        @Override
        @JsonAnySetter
        protected void putDataField(String key, Object value)
        {
            // look through our columns to see which matches
            for (Column column : view.getColumns())
            {
                if (key.equals(column.getFieldName()))
                {
                    data.put(column.getId(), value);
                    return;
                }
            }

            // if nothing matched, it's not a column, so we don't care.
        }

        public SingleRow get(String id, Connection request) throws RequestException
        {
            Response response = request.get(base + path() + "/" + id);
            return result(response, true, SingleRow.class);
        }
    }

    public static class BulkResults
    {
        int rowsUpdated;
        int rowsDeleted;
        int rowsCreated;
        int errors;

        @JsonProperty("Rows Updated")
        public int getRowsUpdated() {
            return rowsUpdated;
        }

        @JsonProperty("Rows Deleted")
        public int getRowsDeleted() {
            return rowsDeleted;
        }

        @JsonProperty("Rows Created")
        public int getRowsCreated() {
            return rowsCreated;
        }

        @JsonProperty("Errors")
        public int getErrors() {
            return errors;
        }

        @JsonProperty("Rows Updated")
        public void setRowsUpdated(int rowsUpdated) {
            this.rowsUpdated = rowsUpdated;
        }

        @JsonProperty("Rows Deleted")
        public void setRowsDeleted(int rowsDeleted) {
            this.rowsDeleted = rowsDeleted;
        }

        @JsonProperty("Rows Created")
        public void setRowsCreated(int rowsCreated) {
            this.rowsCreated = rowsCreated;
        }

        @JsonProperty("Errors")
        public void setErrors(int errors) {
            this.errors = errors;
        }
    }

    String id;
    String name;
    String description;
    Integer rowIdentifierColumnId;
    Long rowsUpdatedAt;
    String displayType;
    Integer viewCount;
    String viewType;
    Map<String, Object> metadata;
    Map<String, Object> privateMetadata;
    List<String> flags = new ArrayList<String>();
    List<String> rights = new ArrayList<String>();
    List<String> tags = new ArrayList<String>();
    List<Column> columns = new ArrayList<Column>();

    private static final String CUSTOM_FIELDS_ID = "custom_fields";

    public String getId()
    {
        return id;
    }

    private void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<Column> getColumns()
    {
        return columns;
    }

    public void setColumns(List<Column> columns)
    {
        this.columns = columns;
    }

    @JsonIgnore
    public Column getColumnById(int id)
    {
        for (Column column : this.columns)
        {
            if (column.id == id)
            {
                return column;
            }
        }
        return null;
    }

    @JsonIgnore
    public Column getColumnByApiIdentifier(String name)
    {
        for (Column column : this.columns)
        {
            if (column.getFieldName().equals(name))
            {
                return column;
            }
        }
        return null;
    }

    private Integer getRowIdentifierColumnId()
    {
        return rowIdentifierColumnId;
    }

    private void setRowIdentifierColumnId(Integer id)
    {
        this.rowIdentifierColumnId = id;
    }

    @JsonIgnore
    public Column getRowIdentifierColumn()
    {
        if (this.rowIdentifierColumnId == null)
        {
            return null;
        }
        return getColumnById(this.rowIdentifierColumnId);
    }

    public void setRowIdentifierColumn(Column column)
    {
        if (column.getView() != this)
        {
            throw new IllegalArgumentException("The column given is not a part of this view.");
        }
        this.rowIdentifierColumnId = column.id;
    }

    public Long getRowsUpdatedAt()
    {
        return rowsUpdatedAt;
    }

    private void setRowsUpdatedAt(Long rowsUpdatedAt)
    {
        this.rowsUpdatedAt = rowsUpdatedAt;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDisplayType()
    {
        return displayType;
    }

    public void setDisplayType(String displayType)
    {
        this.displayType = displayType;
    }

    public Integer getViewCount()
    {
        return viewCount;
    }

    private void setViewCount(Integer viewCount)
    {
        this.viewCount = viewCount;
    }

    public String getViewType()
    {
        return viewType;
    }

    public void setViewType(String viewType)
    {
        this.viewType = viewType;
    }

    public List<String> getFlags()
    {
        return flags;
    }

    public void setFlags(List<String> flags)
    {
        this.flags = flags;
    }

    public List<String> getRights()
    {
        return rights;
    }

    public void setRights(List<String> rights)
    {
        this.rights = rights;
    }

    public List<String> getTags()
    {
        return tags;
    }

    public void setTags(List<String> tags)
    {
        this.tags = tags;
    }

    public Map<String, Object> getMetadata()
    {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata)
    {
        this.metadata = metadata;
    }

    public Map<String, Object> getPrivateMetadata()
    {
        return privateMetadata;
    }

    public void setPrivateMetadata(Map<String, Object> privateMetadata)
    {
        this.privateMetadata = privateMetadata;
    }

    @JsonIgnore
    public Map<String, Object> getCustomMetadataFields(boolean fieldIsPublic)
    {
        Map<String, Object> target = getMetadataContainer(fieldIsPublic);
        if (target == null)
        {
            return null;
        }
        return (Map<String, Object>)target.get(CUSTOM_FIELDS_ID);
    }

    @JsonIgnore
    public Map<String, Object> getCustomMetadataFields()
    {
        return getCustomMetadataFields(true);
    }

    @JsonIgnore
    private Map<String, Object> getDeepValueCreatingAlongTheWay(Map<String, Object> container, String... keys)
    {
        if (container == null)
        {
            throw new IllegalArgumentException("Can't get deep values from a null map");
        }
        for(String key : keys)
        {
            Map<String, Object> child = (Map<String, Object>) container.get(key);
            if (child == null)
            {
                child = new HashMap<String, Object>();
                container.put(key, child);
            }
            container = child;
        }
        return container;
    }

    @JsonIgnore
    private Map<String, Object> getMetadataContainer(boolean isPublic)
    {
        return isPublic ? getMetadata() : getPrivateMetadata();
    }

    /**
     * Sets a custom metadata field's value
     * @param fieldsetName The name of the fieldset
     * @param fieldName The name of the field
     * @param value The value to set
     * @param fieldIsPublic Set on the publicly viewable metadata
     */
    public void setCustomMetadataField(String fieldsetName, String fieldName, String value, boolean fieldIsPublic)
    {
        Map<String, Object> target = getMetadataContainer(fieldIsPublic);
        if (target == null)
        {
            target = new HashMap<String, Object>();
            if (fieldIsPublic)
            {
                setMetadata(target);
            }
            else
            {
                setPrivateMetadata(target);
            }
        }
        Map<String, Object> fieldset = getDeepValueCreatingAlongTheWay(
                target, CUSTOM_FIELDS_ID, fieldsetName);
        fieldset.put(fieldName, value);
    }

    /**
     * Sets a custom metadata field's value on publicly viewable metadata
     * @param fieldset The name of the fieldset
     * @param field The name of the field
     * @param value The value to set
     */
    public void setCustomMetadataField(String fieldset, String field, String value)
    {
        setCustomMetadataField(fieldset, field, value, true);
    }

    @JsonIgnore
    /**
     * Gets a value from the view's custom metadata
     */
    public String getCustomFieldValue(String fieldsetName, String fieldName, boolean fieldIsPublic)
    {
        Map<String, Object> customFields = getCustomMetadataFields(fieldIsPublic);
        if (customFields == null)
        {
            return null;
        }
        return (String)getDeepValueCreatingAlongTheWay(customFields, fieldsetName).get(fieldName);
    }

    @JsonIgnore
    /**
     * Gets a value from the view's custom public metadata
     */
    public String getCustomFieldValue(String fieldsetName, String fieldName)
    {
        return getCustomFieldValue(fieldsetName, fieldName, true);
    }

    /* row operations */

    public Row getRow(int sid, Connection conn) throws RequestException
    {
        return getRow(Integer.toString(sid), conn);
    }

    public Row getRow(String identifier, Connection conn) throws RequestException
    {
        if (identifier == null)
        {
            throw new IllegalArgumentException("A row identifier must be provided to get a row.");
        }

        SingleRow row = new SingleRow();
        row.setView(this);
        row = row.get(identifier, conn);

        return row;
    }

    public List<Row> getRows(int start, int length, Connection conn) throws RequestException
    {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.putSingle("method", "getRows");
        params.putSingle("start", Integer.toString(start));
        params.putSingle("length", Integer.toString(length));
        Response response = conn.post(base + path() + "/" + this.id + "/rows", params);
        return results(response, Row.class);
    }

    public Row appendRow(Row row, Connection conn) throws RequestException
    {
        row = row.clone();
        row.setView(this);
        return row.create(conn);
    }

    public static View find(String id, Connection conn) throws RequestException
    {
        View view = new View().get(id, conn, View.class);
        for (Column col : view.getColumns())
        {
            col.setView(view);
        }

        return view;
    }

    static class Blueprint
    {
        static class BlueprintColumn
        {
            String name;
            String description;
            String datatype;

            public String getName()
            {
                return name;
            }

            public void setName(String name)
            {
                this.name = name;
            }

            public String getDescription()
            {
                return description;
            }

            public void setDescription(String description)
            {
                this.description = description;
            }

            public String getDatatype()
            {
                return datatype;
            }

            public void setDatatype(String datatype)
            {
                this.datatype = datatype;
            }
        }

        String name;
        String description;
        int skip;
        List<BlueprintColumn> columns;

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        public int getSkip()
        {
            return skip;
        }

        public void setSkip(int skip)
        {
            this.skip = skip;
        }

        public List<BlueprintColumn> getColumns()
        {
            return columns;
        }

        public void setColumns(List<BlueprintColumn> columns)
        {
            this.columns = columns;
        }
    }

    public void addColumn(Column col)
    {
        getColumns().add(col);
    }

    /**
     * Create a new dataset.
     */
    @Override
    public View create(Connection conn) throws RequestException
    {
        return create(conn, View.class);
    }

    /**
     * Save the changes to a dataset or view.
     */
    @Override
    public View update(Connection conn) throws RequestException
    {
        return update(conn, true);
    }

    /**
     * Save the changes to a dataset or view.
     */
    public View update(Connection conn, boolean updateColumns) throws RequestException
    {
        if (updateColumns)
        {
            List<Column> mutatableColumns = new ArrayList<Column>();
            for (Column col : getColumns())
            {
                if (col.getId() == 0)
                {
                    // This column hasn't been created before. We can't create
                    // columns by calling PUT /views/<4x4>.json, so we need to
                    // POST these directly to the columns service.
                    col = col.clone();
                    col.setView(this);
                    col = col.create(conn);
                }

                mutatableColumns.add(col);
            }

            // Now that we've created any column that needed to be created, we need
            // to *set* the column temporarily to our ideal set of columns...

            List<Column> unmutableColumns = getColumns();
            try
            {
                setColumns(mutatableColumns);
                return update(getId(), conn, View.class);
            }
            finally
            {
                setColumns(unmutableColumns);
            }
        }
        else
        {
            return update(getId(), conn, View.class);
        }
    }

    /**
     * Delete a dataset or view.
     */
    @Override
    public void delete(Connection conn) throws RequestException
    {
        delete(getId(), conn);
    }

    private String publicationEndpoint() {
      return base + "/views/" + getId() + "/publication";
    }

    /**
     * Publish a dataset. If there's an already published dataset in this
     * publication group, then it will be automatically snapshotted.
     *
     * @return The newly published dataset (the uid might have changed).
     */
    public View publish(Connection conn) throws RequestException
    {
        this.waitForGeocoding(conn);

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.putSingle("viewId", getId());
        Response response = conn.post(publicationEndpoint(), params);
        return result(response, false, View.class);
    }

    /**
     * Copy a dataset asynchronously. This only sends the request to *start* the
     * copy. If the server decides that the copy is a deferred job, then when
     * this method returns the copy MIGHT NOT ACTUALLY BE FINISHED (thus the
     * void return type).
     */
    public void copyAsync(Connection conn) throws RequestException
    {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.putSingle("viewId", getId());
        params.putSingle("method", "copy");

        Response response = conn.post(publicationEndpoint(), params);
        verifyResponseCode(response);
    }

    /**
     * Copy a dataset synchronously. This sends the request to start the copy
     * and continues asking the server whether or not the copy has been completed
     * until it finally is.
     *
     * @return The dataset that was created.
     */
    public View copy(Connection conn) throws RequestException
    {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.putSingle("viewId", getId());
        params.putSingle("method", "copy");

        Response response = conn.post(publicationEndpoint(), params);
        while (response.status == 202)
        {
            try { Thread.sleep(ticketCheck); } catch (InterruptedException e) {}

            response = conn.get(publicationEndpoint(), params);
        }

        View v = result(response, false, View.class);

        // Refetch the view to get its full metadata
        return View.find(v.getId(), conn);
    }

    /**
     * Copy a dataset synchronously. Behaves like copy(connection), except
     * that it does not bother to copy the actual data in the source dataset.
     * Useful if the operation you're immediately going to perform is a full
     * row-replace anyway.
     *
     * @return The dataset that was created.
     */
    public View copySchema(Connection conn) throws RequestException
    {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.putSingle("viewId", getId());
        params.putSingle("method", "copySchema");

        Response response = conn.post(publicationEndpoint(), params);
        return result(response, false, View.class);
    }

    /**
     * Scans a file for import and returns the report as a map. The most
     * important piece of the result is the "fileId" field which is the sha256
     * of the file's contents. This is used by the import methods to tell the
     * socrata service what file to use for import.
     *
     * @return The report generated by the file scan.
     */
    Map<String, Object> scan(File file, Connection conn) throws RequestException
    {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.putSingle("method", "scan");
        Response response = conn.post(base + "/imports2", params, file);
        verifyResponseCode(response);

        try
        {
            ObjectMapper mapper = new ObjectMapper();
            return (Map<String, Object>)mapper.readValue(response.body, Object.class);
        }
        catch (IOException e)
        {
            throw new RequestException("Unable to parse the report.", e);
        }
    }

    /**
     * Create a new list of blueprint columns from the view columns.
     *
     * @return The dataset's columns as blueprint columns for use when creating
     *          a new dataset on import.
     */
    List<Blueprint.BlueprintColumn> blueprintColumns()
    {
        List<Blueprint.BlueprintColumn> cols = new ArrayList<Blueprint.BlueprintColumn>();
        for (Column viewCol : getColumns())
        {
            Blueprint.BlueprintColumn bpCol = new Blueprint.BlueprintColumn();
            bpCol.setName(viewCol.getName());
            bpCol.setDescription(viewCol.getDescription());
            bpCol.setDatatype(viewCol.getDataTypeName());
            cols.add(bpCol);
        }

        return cols;
    }

    /**
     * Create a blueprint from this dataset to describe how to import it.
     *
     * @return The dataset's blueprint
     */
    Blueprint blueprint()
    {
        Blueprint bp = new Blueprint();
        bp.setName(getName());
        bp.setDescription(getDescription());
        bp.setSkip(0); // TODO: Configure this somewhere?
        bp.setColumns(blueprintColumns());

        return bp;
    }

    /**
     * Create a dataset by importing a file as it's rows.
     *
     * @return The dataset that was created.
     */
    public View create(File file, Connection conn) throws RequestException
    {
        return create(file, "", conn);
    }

    /**
     * Create a dataset by importing a file as it's rows with a translation. For
     * details on the translation, please see the publisher API docs.
     *
     * @return The dataset that was created.
     */
    public View create(File file, String translation, Connection conn) throws RequestException
    {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.putSingle("name", file.getName());
        params.putSingle("translation", translation);

        ObjectMapper mapper = new ObjectMapper();
        try
        {
            params.putSingle("blueprint", mapper.writeValueAsString(blueprint()));
        }
        catch (IOException e)
        {
            throw new RequestException("Unable to ", e);
        }

        return importinate(params, file, conn);
    }

    public int getPendingGeocodingRequests(Connection conn) throws RequestException
    {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.putSingle("method", "pending");

        Response response = conn.get(base + "/geocoding/" + this.getId(), params);
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            Map<String, Object> result = (Map<String, Object>)mapper.readValue(response.body, Object.class);
            return (Integer)result.get("view");
        }
        catch (IOException ex)
        {
            throw new RequestException("Unable to parse the response.", ex);
        }
    }

    public void waitForGeocoding(Connection conn) throws RequestException
    {
        int pending = this.getPendingGeocodingRequests(conn);
        while (pending > 0)
        {
            try { Thread.sleep(ticketCheck); } catch (InterruptedException e) {}
            pending = this.getPendingGeocodingRequests(conn);
        }
    }

    /**
     * Append rows to a dataset from a file.
     */
    public View append(File file, Connection conn) throws RequestException
    {
        return append(file, 0, "", conn);
    }

    /**
     * Append rows to a dataset from a file.
     * Skip indicates the number of rows to ignore on account of header data.
     */
    public View append(File file, int skip, Connection conn) throws RequestException
    {
        return append(file, skip, "", conn);
    }

    /**
     * Append rows to a dataset from a file. For details on the translation,
     * please see the publisher API docs.
     */
    public View append(File file, int skip, String translation, Connection conn) throws RequestException
    {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.putSingle("method", "append");
        params.putSingle("viewUid", getId());
        params.putSingle("name", file.getName());
        params.putSingle("translation", translation);
        params.putSingle("skip", Integer.toString(skip));

        return importinate(params, file, conn);
    }

    /**
     * Replace rows in a dataset (first delete, then append) from a file.
     */
    public View replace(File file, Connection conn) throws RequestException
    {
        return replace(file, 0, "", conn);
    }

    /**
     * Replace rows in a dataset (first delete, then append) from a file.
     * Skip indicates the number of rows to ignore on account of header data.
     */
    public View replace(File file, int skip, Connection conn) throws RequestException
    {
        return replace(file, skip, "", conn);
    }

    /**
     * Replace rows in a dataset (first delete, then append) from a file. For
     * details on the translation, please see the publisher API docs.
     */
    public View replace(File file, int skip, String translation, Connection conn) throws RequestException
    {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.putSingle("method", "replace");
        params.putSingle("viewUid", getId());
        params.putSingle("name", file.getName());
        params.putSingle("translation", translation);
        params.putSingle("skip", Integer.toString(skip));

        return importinate(params, file, conn);
    }

    View importinate(MultivaluedMap<String, String> params, File file, Connection conn) throws RequestException
    {
        Map<String, Object> report = scan(file, conn);
        String fileId = (String)report.get("fileId");
        params.putSingle("fileId", fileId);

        Response response = conn.post(base + "/imports2", params);
        while (response.status == 202)
        {
            try { Thread.sleep(ticketCheck); } catch (InterruptedException e) {}

            try
            {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> ticket = (Map<String, Object>)mapper.readValue(response.body, Object.class);

                // TODO: Error checking.
                params.putSingle("ticket", ticket.get("ticket").toString());
            }
            catch (IOException e)
            {
                throw new RequestException("Unable to parse the report.", e);
            }

            response = conn.get(base + "/imports2", params);
        }

        return result(response, false, View.class);
    }

    public BulkResults upsert(List<NewRow> records, Connection conn) throws RequestException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Response response = conn.post(base + "/id/" + this.id, mapper.writeValueAsString(records));
            verifyResponseCode(response);

            return mapper.readValue(response.body, BulkResults.class);
        } catch (IOException e) {
            throw new RequestException("Unexpected IOException while submitting:", e);
        }
    }


}
