package org.odata4j.expression;

public interface ExpressionVisitor {

	public void beforeDescend();
	public void afterDescend();
	public void betweenDescend();
	public void visit(String type);

	
	public void visit(OrderByExpression expr);
	
	
	public void visit(AddExpression expr);
	public void visit(AndExpression expr);
	public void visit(BooleanLiteral expr);
	public void visit(CastExpression expr);
	public void visit(ConcatMethodCallExpression expr);
	public void visit(DateTimeLiteral expr);
	public void visit(DateTimeOffsetLiteral expr);
	public void visit(DecimalLiteral expr);
	public void visit(DivExpression expr);
	public void visit(EndsWithMethodCallExpression expr);
	public void visit(EntitySimpleProperty expr);
	public void visit(EqExpression expr);
	public void visit(GeExpression expr);
	public void visit(GtExpression expr);
	public void visit(GuidLiteral expr);
	public void visit(BinaryLiteral expr);
	public void visit(IndexOfMethodCallExpression expr);
	public void visit(SingleLiteral expr);
	public void visit(DoubleLiteral expr);
	public void visit(IntegralLiteral expr);
	public void visit(Int64Literal expr);
	public void visit(IsofExpression expr);
	public void visit(LeExpression expr);
	public void visit(LengthMethodCallExpression expr);
	public void visit(LtExpression expr);
	public void visit(ModExpression expr);
	public void visit(MulExpression expr);
	public void visit(NeExpression expr);
	public void visit(NegateExpression expr);
	public void visit(NotExpression expr);
	public void visit(NullLiteral expr);
	public void visit(OrExpression expr);
	public void visit(ParenExpression expr);
	public void visit(ReplaceMethodCallExpression expr);
	public void visit(StartsWithMethodCallExpression expr);
	public void visit(StringLiteral expr);
	public void visit(SubExpression expr);
	public void visit(SubstringMethodCallExpression expr);
	public void visit(SubstringOfMethodCallExpression expr);
	public void visit(TimeLiteral expr);
	public void visit(ToLowerMethodCallExpression expr);
	public void visit(ToUpperMethodCallExpression expr);
	public void visit(TrimMethodCallExpression expr);
	public void visit(YearMethodCallExpression expr);
	public void visit(MonthMethodCallExpression expr);
	public void visit(DayMethodCallExpression expr);
	public void visit(HourMethodCallExpression expr);
	public void visit(MinuteMethodCallExpression expr);
	public void visit(SecondMethodCallExpression expr);
	public void visit(RoundMethodCallExpression expr);
	public void visit(FloorMethodCallExpression expr);
	public void visit(CeilingMethodCallExpression expr);
	
	
	
	

	
	
}
