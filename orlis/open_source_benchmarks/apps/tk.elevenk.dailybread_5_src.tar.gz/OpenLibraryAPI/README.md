#[OpenLibrary API](https://openlibrary.org/developers/api) Java Interface

This Java interface was written for use in the Android app [DailybRead](https://github.com/jpkrause/DailybRead), but was developed in a manner that would make it usable by any Java project as there are no Android specific dependencies.

The interface is relatively simple and easy to use. It is still in development but is ready to be used as it is.

The **LibraryAPI** interface has an work-in-progress implementation **OpenLibAPI** which has the following currently implemented:

    /**
     * Executes a Book API call 
     * @param data Map of query values
     * @return JSON data from API
     */
    public JSONObject books(Map data);

    /**
     * Executes a Covers API call 
     * @param data Map of the query values
     * @return JSON data from API
     */
    public JSONObject covers(Map data);

    /**
     * Executes a Lists API call 
     * @param data Map of query values
     * @return JSON data from api
     */
    public JSONObject lists(Map data);

    /**
     * Executes a Read API call 
     * @param data Map of query values
     * @return JSON data from API
     */
    public JSONObject read(Map data);

    /**
     * Executes a Recent Changes API call 
     * @param data Map of query values
     * @return JSON data from API
     */
    public JSONObject recentChanges(Map data);

    /**
     * Executes a Search API call 
     * @param data Map of query values
     * @return JSON data from server
     */
    public JSONObject search(Map data);

    /**
     * Executes a SearchInside API call 
     * @param data Map of query values
     * @return JSON data from API
     */
    public JSONObject searchInside(Map data);

    /**
     * Executes a Subject API call 
     * @param data Map of query values
     * @return JSON data from API
     */
    public JSONObject subjects(Map data);

    /**
     * Executes a Login API call 
     * @param data Map of query values
     * @return Cookie containing session data
     */
    public Cookie login(Map data);

This interface is meant to be used though the **Library** interface.

The **Library** interface has a work-in-process implementation, **OpenLibrary** which has the following currently implemented:

    /**
      * Executes a search using the given SearchQuery 
      * @param search The SearchQuery object that has the search parameters to be used
      * @return A list of resuts lwrapped in the SearchResults class
      */
     public SearchResults search(SearchQuery search);
 
     /**
      * Gets the data from the OpenLibrary Book API and returns it
      * wrapped in a Book object 
      * @param bookQuery The query to use for the data retrieval
      * @return A BookData object with all of the data from the API
      */
     public BookData getBookDetails(BookQuery bookQuery);
 
     /**
      *
      * Gets the data from the OpenLibrary Read API and returns it
      * wrapped in a Book object
      * @param readQuery The query to use for the data retrieval
      * @return A BookData object with all of the data from the API
      */
     public BookData getReadingDetails(ReadQuery readQuery);
 
     /**
      * Gets the eBook file from the given BookData. Currently
      * only implemented to get the epub file 
      * @param bookData A Book to get the eBook from
      * @return The eBook, usually a File object that can be opened by a reader or saved
      */
     public Object getEbook(BookData bookData);
 
     /**
      * Searches the library database for a random book using
      * a random word generating service to execute searches.
      * The calling class must implement the callbacks interface
      * in order to get updates on the status of the search 
      * @param callbacks The interface that will receive the updates
      * @return A book object, usually a BookData class with the info
      *      about the book 
      */
     public Object findRandomBook(LibraryCallbacks callbacks);
 
     /**
      * Logs the user in to OpenLibrary
      * @param creds
      * @return
      */
     public boolean login(Credential creds);
 
     /**
      * Gets the small, medium and large cover urls for a book.
      * @param key The search key, either ISBN, OCLC, LCCN, OLID or ID
      * @param value The value associated with the key
      * @return A map of the cover urls with keys 'small', 'medium' and 'large'
      */
     public Map getCoverUrls(Object key, Object value);
     
For more information on the Interface, see the code itself.
The Library interface uses the LibraryApi interface to actually make the requests and get the responses.
See the LibraryApi interface for more info. Note: not all functionality in the interfaces has actually been implemented.
The BookData object has a huge amount of accessors and calls that it can make to get data about specific books.

If you want to help, feel free to submit any requests or issues, or fork it and make the change for all of us!