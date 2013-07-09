package com.nascentdigital.communication;

import java.util.Map;
import org.json.JSONObject;
import org.w3c.dom.Document;
import com.google.gson.JsonElement;


public abstract class ServiceResponseFormat<T>
{
	// [region] constants

	public static final ServiceResponseFormat<byte[]> RAW = new RawFormat();
	public static final ServiceResponseFormat<String> STRING = new StringFormat();
	public static final ServiceResponseFormat<Map<String, String>> FORM_ENCODED =
		new FormEncodedFormat();
	public static final ServiceResponseFormat<JSONObject> JSON = new JsonFormat();
	public static final ServiceResponseFormat<JsonElement> GSON = new GsonFormat();
	public static final ServiceResponseFormat<Document> XML = new XmlFormat();

	// [endregion]


	// [region] instance variables

	protected final Type type;

	// [endregion]


	// [region] constructors

	private ServiceResponseFormat(Type type)
	{
		this.type = type;
	}

	// [endregion]


	// [region] internal data structures

	protected enum Type
	{
		RAW, STRING, FORM_ENCODED, JSON, GSON, XML

	} // Type

	private static final class RawFormat extends ServiceResponseFormat<byte[]>
	{

		// [region] constructors

		private RawFormat()
		{
			super(Type.RAW);
		}

		// [endregion]

	} // class RawFormat

	private static final class StringFormat extends ServiceResponseFormat<String>
	{

		// [region] constructors

		private StringFormat()
		{
			super(Type.STRING);
		}

		// [endregion]

	} // class StringFormat

	private static final class FormEncodedFormat extends
		ServiceResponseFormat<Map<String, String>>
	{

		// [region] constructors

		private FormEncodedFormat()
		{
			super(Type.FORM_ENCODED);
		}

		// [endregion]

	} // class FormEncodedFormat

	private static final class JsonFormat extends ServiceResponseFormat<JSONObject>
	{

		// [region] constructors

		private JsonFormat()
		{
			super(Type.JSON);
		}

		// [endregion]

	} // class JsonFormat

	private static final class GsonFormat extends ServiceResponseFormat<JsonElement>
	{

		// [region] constructors

		private GsonFormat()
		{
			super(Type.GSON);
		}

		// [endregion]

	} // class GsonFormat
	
	private static final class XmlFormat extends ServiceResponseFormat<Document>
	{

		// [region] constructors

		private XmlFormat()
		{
			super(Type.XML);
		}

		// [endregion]

	} // class GsonFormat

	// [endregion]

} // class ResponseFormat