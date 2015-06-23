package com.nascentdigital.communication;

import com.nascentdigital.communication.ServiceClient.MultiPartDataType;


public class MultiPartEntity
{
    // [region] constants

    // [endregion]


    // [region] instance variables
    String name;
    String contentType;
    String content;

    //Following used when uploading files
    MultiPartDataType dataType;
    Object fileContent;
    String filename;
    // [endregion]


    // [region] constructors

    /**
     * @param name
     * @param contentType
     * @param content
     */
    public MultiPartEntity(String _name, String _contentType, String _content)
    {
        name = _name;
        contentType = _contentType;
        content = _content;
    }

    // [endregion]
    /**
     * @param name
     * @param contentType
     * @param dataType
     * @param fileContent
     * @param filename
     */
    public MultiPartEntity(String _name, String _contentType, String _filename,
        MultiPartDataType _dataType, Object _fileContent)
    {
        name = _name;
        contentType = _contentType;
        filename = _filename;
        dataType = _dataType;
        fileContent = _fileContent;
    }


    // [region] properties

    // [endregion]


    // [region] helper methods

    // [endregion]

}
