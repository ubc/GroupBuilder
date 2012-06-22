package ca.ubc.ctlt.group.groupcreator;

public class SearchCriteria
{
	private String field;
	private String op;
	private String term;
	/**
	 * @param field
	 * @param op
	 * @param term
	 */
	public SearchCriteria(String field, String op, String term)
	{
		super();
		this.field = field;
		this.op = op;
		this.term = term;
	}
	/**
	 * @return the field
	 */
	public String getField()
	{
		return field;
	}
	/**
	 * @return the op
	 */
	public String getOp()
	{
		return op;
	}
	/**
	 * @return the term
	 */
	public String getTerm()
	{
		return term;
	}

}
