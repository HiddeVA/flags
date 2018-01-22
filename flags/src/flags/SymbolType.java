package flags;

public enum SymbolType {
	CIRCLE,
	STAR,
	CRESCENT,
	OTHER;
	
	public String toString()
	{
		String str = super.toString();
		return str.substring(0, 1) + str.substring(1).toLowerCase();
	}
}
