package com.socrata.data;

import com.socrata.api.Connection;
import com.socrata.api.RequestException;
import com.socrata.api.Response;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        int id;
        String name;
        String dataTypeName;
        int width;
        View view;

        public int getId()
        {
            return id;
        }

        public void setId(int id)
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

        public String getDataTypeName()
        {
            return dataTypeName;
        }

        public void setDataTypeName(String dataTypeName)
        {
            this.dataTypeName = dataTypeName;
        }

        public int getWidth()
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

        void setView(View view)
        {
            this.view = view;
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
            clone.setWidth(getWidth());

            return clone;
        }
    }

    String id;
    String name;
    String description;
    Long rowsUpdatedAt;
    String displayType;
    Integer viewCount;
    String viewType;
    List<String> flags = new ArrayList<String>();
    List<String> rights = new ArrayList<String>();
    List<String> tags = new ArrayList<String>();
    List<Column> columns = new ArrayList<Column>();

    public String getId()
    {
        return id;
    }

    public void setId(String id)
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

    public Long getRowsUpdatedAt()
    {
        return rowsUpdatedAt;
    }

    public void setRowsUpdatedAt(Long rowsUpdatedAt)
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

    public void setViewCount(Integer viewCount)
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
            String datatype;

            public String getName()
            {
                return name;
            }

            public void setName(String name)
            {
                this.name = name;
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

    /**
     * Delete a dataset or view.
     */
    @Override
    public void delete(Connection conn) throws RequestException
    {
        delete(getId(), conn);
    }

    /**
     * Publish a dataset. If there's an already published dataset in this
     * publication group, then it will be automatically snapshotted.
     *
     * @return The newly published dataset (the uid might have changed).
     */
    public View publish(Connection conn) throws RequestException
    {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.putSingle("viewId", getId());
        Response response = conn.post(base + "/publication", params);
        return results(response, View.class);
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

        Response response = conn.post(base + "/publication", params);
        validate(response);
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

        Response response = conn.post(base + "/publication", params);
        while (response.status == 202)
        {
            try { Thread.sleep(1000l); } catch (InterruptedException e) {}

            response = conn.get(base + "/publication", params);
        }

        return results(response, View.class);
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
        validate(response);

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

    /**
     * Append rows to a dataset from a file.
     */
    public View append(File file, Connection conn) throws RequestException
    {
        return append(file, "", conn);
    }

    /**
     * Append rows to a dataset from a file. For details on the translation,
     * please see the publisher API docs.
     */
    public View append(File file, String translation, Connection conn) throws RequestException
    {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.putSingle("method", "append");
        params.putSingle("viewUid", getId());
        params.putSingle("name", file.getName());
        params.putSingle("translation", translation);
        params.putSingle("skip", "0"); // TODO: Where should this plumb through?

        return importinate(params, file, conn);
    }

    /**
     * Replace rows in a dataset (first delete, then append) from a file.
     */
    public View replace(File file, Connection conn) throws RequestException
    {
        return replace(file, "", conn);
    }

    /**
     * Replace rows in a dataset (first delete, then append) from a file. For
     * details on the translation, please see the publisher API docs.
     */
    public View replace(File file, String translation, Connection conn) throws RequestException
    {
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.putSingle("method", "replace");
        params.putSingle("viewUid", getId());
        params.putSingle("name", file.getName());
        params.putSingle("translation", translation);
        params.putSingle("skip", "0"); // TODO: Where should this plumb through?

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
            try { Thread.sleep(1000l); } catch (InterruptedException e) {}

            try
            {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> ticket = (Map<String, Object>)mapper.readValue(response.body, Object.class);

                // TODO: Error checking.
                params.putSingle("ticket", ticket.get("id").toString());
            }
            catch (IOException e)
            {
                throw new RequestException("Unable to parse the report.", e);
            }

            response = conn.get(base + "/imports2", params);
        }

        return results(response, View.class);
    }
}