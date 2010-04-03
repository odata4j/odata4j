package org.odata4j.expression;

import java.math.BigDecimal;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

public class Expression {


	public static NullLiteral null_(){
		return new NullLiteral(){ };
	}
	public static IntegralLiteral integral(final long value){
		return new IntegralLiteral(){

			@Override
			public long getValue() {
				return value;
			}};
	}
	public static BooleanLiteral boolean_(final boolean value){
		return new BooleanLiteral(){

			@Override
			public boolean getValue() {
				return value;
			}};
	}
	public static DateTimeLiteral dateTime(final LocalDateTime value){
		return new DateTimeLiteral(){

			@Override
			public LocalDateTime getValue() {
				return value;
			}};
	}
	public static DateTimeOffsetLiteral dateTimeOffset(final DateTime value){
		return new DateTimeOffsetLiteral(){

			@Override
			public DateTime getValue() {
				return value;
			}};
	}
	public static TimeLiteral time(final LocalTime value){
		return new TimeLiteral(){

			@Override
			public LocalTime getValue() {
				return value;
			}};
	}
	public static StringLiteral string(final String value){
		return new StringLiteral(){

			@Override
			public String getValue() {
				return value;
			}};
	}
	public static GuidLiteral guid(final UUID value){
		return new GuidLiteral(){

			@Override
			public UUID getValue() {
				return value;
			}};
	}
	public static DecimalLiteral decimal(final BigDecimal value){
		return new DecimalLiteral(){

			@Override
			public BigDecimal getValue() {
				return value;
			}};
	}
	public static BinaryLiteral binary(final byte[] value){
		return new BinaryLiteral(){

			@Override
			public byte[] getValue() {
				return value;
			}};
	}
	public static SingleLiteral single(final float value){
		return new SingleLiteral(){

			@Override
			public float getValue() {
				return value;
			}};
	}
	public static DoubleLiteral double_(final double value){
		return new DoubleLiteral(){

			@Override
			public double getValue() {
				return value;
			}};
	}
	public static Int64Literal int64(final long value){
		return new Int64Literal(){

			@Override
			public long getValue() {
				return value;
			}};
	}
	public static EntitySimpleProperty simpleProperty(final String propertyName){
		return new EntitySimpleProperty(){

			@Override
			public String getPropertyName() {
				return propertyName;
			}};
	}
	
	public static EqExpression eq(final CommonExpression lhs, final CommonExpression rhs){
		return new EqExpression() {
			
			@Override
			public CommonExpression getRHS() {
				return rhs;
			}
			
			@Override
			public CommonExpression getLHS() {
				return lhs;
			}
		};
	}
	public static NeExpression ne(final CommonExpression lhs, final CommonExpression rhs){
		return new NeExpression() {
			
			@Override
			public CommonExpression getRHS() {
				return rhs;
			}
			
			@Override
			public CommonExpression getLHS() {
				return lhs;
			}
		};
	}
	public static AndExpression and(final BoolCommonExpression lhs, final BoolCommonExpression rhs){
		return new AndExpression() {
			
			@Override
			public BoolCommonExpression getRHS() {
				return rhs;
			}
			
			@Override
			public BoolCommonExpression getLHS() {
				return lhs;
			}
		};
	}
	public static OrExpression or(final BoolCommonExpression lhs, final BoolCommonExpression rhs){
		return new OrExpression() {
			
			@Override
			public BoolCommonExpression getRHS() {
				return rhs;
			}
			
			@Override
			public BoolCommonExpression getLHS() {
				return lhs;
			}
		};
	}
	public static LtExpression lt(final CommonExpression lhs, final CommonExpression rhs){
		return new LtExpression() {
			
			@Override
			public CommonExpression getRHS() {
				return rhs;
			}
			
			@Override
			public CommonExpression getLHS() {
				return lhs;
			}
		};
	}
	public static GtExpression gt(final CommonExpression lhs, final CommonExpression rhs){
		return new GtExpression() {
			
			@Override
			public CommonExpression getRHS() {
				return rhs;
			}
			
			@Override
			public CommonExpression getLHS() {
				return lhs;
			}
		};
	}
	public static LeExpression le(final CommonExpression lhs, final CommonExpression rhs){
		return new LeExpression() {
			
			@Override
			public CommonExpression getRHS() {
				return rhs;
			}
			
			@Override
			public CommonExpression getLHS() {
				return lhs;
			}
		};
	}
	public static GeExpression ge(final CommonExpression lhs, final CommonExpression rhs){
		return new GeExpression() {
			
			@Override
			public CommonExpression getRHS() {
				return rhs;
			}
			
			@Override
			public CommonExpression getLHS() {
				return lhs;
			}
		};
	}
	public static AddExpression add(final CommonExpression lhs, final CommonExpression rhs){
		return new AddExpression() {
			
			@Override
			public CommonExpression getRHS() {
				return rhs;
			}
			
			@Override
			public CommonExpression getLHS() {
				return lhs;
			}
		};
	}
	public static SubExpression sub(final CommonExpression lhs, final CommonExpression rhs){
		return new SubExpression() {
			
			@Override
			public CommonExpression getRHS() {
				return rhs;
			}
			
			@Override
			public CommonExpression getLHS() {
				return lhs;
			}
		};
	}
	public static MulExpression mul(final CommonExpression lhs, final CommonExpression rhs){
		return new MulExpression() {
			
			@Override
			public CommonExpression getRHS() {
				return rhs;
			}
			
			@Override
			public CommonExpression getLHS() {
				return lhs;
			}
		};
	}
	public static DivExpression div(final CommonExpression lhs, final CommonExpression rhs){
		return new DivExpression() {
			
			@Override
			public CommonExpression getRHS() {
				return rhs;
			}
			
			@Override
			public CommonExpression getLHS() {
				return lhs;
			}
		};
	}
	public static ModExpression mod(final CommonExpression lhs, final CommonExpression rhs){
		return new ModExpression() {
			
			@Override
			public CommonExpression getRHS() {
				return rhs;
			}
			
			@Override
			public CommonExpression getLHS() {
				return lhs;
			}
		};
	}
	public static ParenExpression paren(final CommonExpression expression){
		return new ParenExpression() {

			@Override
			public CommonExpression getExpression() {
				return expression;
			}
			
		};
	}
	
	public static NotExpression not(final CommonExpression expression){
		return new NotExpression() {

			@Override
			public CommonExpression getExpression() {
				return expression;
			}
			
		};
	}
	
	public static NegateExpression negate(final CommonExpression expression){
		return new NegateExpression() {

			@Override
			public CommonExpression getExpression() {
				return expression;
			}
			
		};
	}
	
	
	
	
	
	public static CastExpression cast(String type){
		return cast(null,type);
	}
	public static CastExpression cast(final CommonExpression expression, final String type){
		return new CastExpression(){

			@Override
			public CommonExpression getExpression() {
				return expression;
			}

			@Override
			public String getType() {
				return type;
			}};
	}
	
	public static IsofExpression isof(String type){
		return isof(null,type);
	}
	public static IsofExpression isof(final CommonExpression expression, final String type){
		return new IsofExpression(){

			@Override
			public CommonExpression getExpression() {
				return expression;
			}

			@Override
			public String getType() {
				return type;
			}};
	}
	
	public static EndsWithMethodCallExpression endsWith(final CommonExpression target, final CommonExpression value){
		return new EndsWithMethodCallExpression(){

			@Override
			public CommonExpression getTarget() {
				return target;
			}

			@Override
			public CommonExpression getValue() {
				return value;
			}};
	}
	public static StartsWithMethodCallExpression startsWith(final CommonExpression target, final CommonExpression value){
		return new StartsWithMethodCallExpression(){

			@Override
			public CommonExpression getTarget() {
				return target;
			}

			@Override
			public CommonExpression getValue() {
				return value;
			}};
	}
	public static SubstringOfMethodCallExpression substringOf(CommonExpression value){
		return substringOf(value,null);
	}
	public static SubstringOfMethodCallExpression substringOf(final CommonExpression value, final CommonExpression target){
		return new SubstringOfMethodCallExpression(){

			@Override
			public CommonExpression getTarget() {
				return target;
			}

			@Override
			public CommonExpression getValue() {
				return value;
			}};
	}
	public static IndexOfMethodCallExpression indexOf(final CommonExpression target, final CommonExpression value){
		return new IndexOfMethodCallExpression(){

			@Override
			public CommonExpression getTarget() {
				return target;
			}

			@Override
			public CommonExpression getValue() {
				return value;
			}};
	}
	public static ReplaceMethodCallExpression replace(final CommonExpression target, final CommonExpression find, final CommonExpression replace){
		return new ReplaceMethodCallExpression(){

			@Override
			public CommonExpression getFind() {
				return find;
			}

			@Override
			public CommonExpression getReplace() {
				return replace;
			}

			@Override
			public CommonExpression getTarget() {
				return target;
			}};
	}
	public static ToLowerMethodCallExpression toLower(final CommonExpression target){
		return new ToLowerMethodCallExpression(){
			
			@Override
			public CommonExpression getTarget() {
				return target;
			}};
	}
	public static ToUpperMethodCallExpression toUpper(final CommonExpression target){
		return new ToUpperMethodCallExpression(){
			
			@Override
			public CommonExpression getTarget() {
				return target;
			}};
	}
	public static TrimMethodCallExpression trim(final CommonExpression target){
		return new TrimMethodCallExpression(){
			
			@Override
			public CommonExpression getTarget() {
				return target;
			}};
	}
	public static SubstringMethodCallExpression substring(final CommonExpression target, final CommonExpression start){
		return substring(target,start,null);
	}
	public static SubstringMethodCallExpression substring(final CommonExpression target, final CommonExpression start, final CommonExpression length){
		return new SubstringMethodCallExpression(){
			
			@Override
			public CommonExpression getTarget() {
				return target;
			}

			@Override
			public CommonExpression getLength() {
				return length;
			}

			@Override
			public CommonExpression getStart() {
				return start;
			}};
	}
	public static ConcatMethodCallExpression concat(final CommonExpression lhs, final CommonExpression rhs){
		return new ConcatMethodCallExpression(){

			@Override
			public CommonExpression getLHS() {
				return lhs;
			}

			@Override
			public CommonExpression getRHS() {
				return rhs;
			}};
	}
	public static LengthMethodCallExpression length(final CommonExpression target){
		return new LengthMethodCallExpression(){
			
			@Override
			public CommonExpression getTarget() {
				return target;
			}};
	}
	public static YearMethodCallExpression year(final CommonExpression target){
		return new YearMethodCallExpression(){
			
			@Override
			public CommonExpression getTarget() {
				return target;
			}};
	}
	public static MonthMethodCallExpression month(final CommonExpression target){
		return new MonthMethodCallExpression(){
			
			@Override
			public CommonExpression getTarget() {
				return target;
			}};
	}
	public static DayMethodCallExpression day(final CommonExpression target){
		return new DayMethodCallExpression(){
			
			@Override
			public CommonExpression getTarget() {
				return target;
			}};
	}
	public static HourMethodCallExpression hour(final CommonExpression target){
		return new HourMethodCallExpression(){
			
			@Override
			public CommonExpression getTarget() {
				return target;
			}};
	}
	public static MinuteMethodCallExpression minute(final CommonExpression target){
		return new MinuteMethodCallExpression(){
			
			@Override
			public CommonExpression getTarget() {
				return target;
			}};
	}
	public static SecondMethodCallExpression second(final CommonExpression target){
		return new SecondMethodCallExpression(){
			
			@Override
			public CommonExpression getTarget() {
				return target;
			}};
	}
	public static RoundMethodCallExpression round(final CommonExpression target){
		return new RoundMethodCallExpression(){
			
			@Override
			public CommonExpression getTarget() {
				return target;
			}};
	}
	public static CeilingMethodCallExpression ceiling(final CommonExpression target){
		return new CeilingMethodCallExpression(){
			
			@Override
			public CommonExpression getTarget() {
				return target;
			}};
	}
	public static FloorMethodCallExpression floor(final CommonExpression target){
		return new FloorMethodCallExpression(){
			
			@Override
			public CommonExpression getTarget() {
				return target;
			}};
	}
	public static OrderByExpression orderBy(final CommonExpression expression, final boolean isAscending){
		return new OrderByExpression(){

			@Override
			public CommonExpression getExpression() {
				return expression;
			}

			@Override
			public boolean isAscending() {
				return isAscending;
			} };
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static void visit(CommonExpression expr, ExpressionVisitor visitor){
		
	
		if (expr instanceof EqExpression){
			visitor.visit((EqExpression)expr);
			visitor.beforeDescend();
			visit(((EqExpression)expr).getLHS(),visitor);
			visitor.betweenDescend();
			visit(((EqExpression)expr).getRHS(),visitor);
			visitor.afterDescend();
		} 
		else if (expr instanceof AndExpression){
			visitor.visit((AndExpression)expr);
			visitor.beforeDescend();
			visit(((AndExpression)expr).getLHS(),visitor);
			visitor.betweenDescend();
			visit(((AndExpression)expr).getRHS(),visitor);
			visitor.afterDescend();
		} 
		else if (expr instanceof OrExpression){
			visitor.visit((OrExpression)expr);
			visitor.beforeDescend();
			visit(((OrExpression)expr).getLHS(),visitor);
			visitor.betweenDescend();
			visit(((OrExpression)expr).getRHS(),visitor);
			visitor.afterDescend();
		}
		else if (expr instanceof NeExpression){
			visitor.visit((NeExpression)expr);
			visitor.beforeDescend();
			visit(((NeExpression)expr).getLHS(),visitor);
			visitor.betweenDescend();
			visit(((NeExpression)expr).getRHS(),visitor);
			visitor.afterDescend();
		}
		else if (expr instanceof LtExpression){
			visitor.visit((LtExpression)expr);
			visitor.beforeDescend();
			visit(((LtExpression)expr).getLHS(),visitor);
			visitor.betweenDescend();
			visit(((LtExpression)expr).getRHS(),visitor);
			visitor.afterDescend();
		} 
		else if (expr instanceof GtExpression){
			visitor.visit((GtExpression)expr);
			visitor.beforeDescend();
			visit(((GtExpression)expr).getLHS(),visitor);
			visitor.betweenDescend();
			visit(((GtExpression)expr).getRHS(),visitor);
			visitor.afterDescend();
		} 
		else if (expr instanceof LeExpression){
			visitor.visit((LeExpression)expr);
			visitor.beforeDescend();
			visit(((LeExpression)expr).getLHS(),visitor);
			visitor.betweenDescend();
			visit(((LeExpression)expr).getRHS(),visitor);
			visitor.afterDescend();
		} 
		else if (expr instanceof GeExpression){
			visitor.visit((GeExpression)expr);
			visitor.beforeDescend();
			visit(((GeExpression)expr).getLHS(),visitor);
			visitor.betweenDescend();
			visit(((GeExpression)expr).getRHS(),visitor);
			visitor.afterDescend();
		} 
		else if (expr instanceof AddExpression){
			visitor.visit((AddExpression)expr);
			visitor.beforeDescend();
			visit(((AddExpression)expr).getLHS(),visitor);
			visitor.betweenDescend();
			visit(((AddExpression)expr).getRHS(),visitor);
			visitor.afterDescend();
		} 
		else if (expr instanceof SubExpression){
			visitor.visit((SubExpression)expr);
			visitor.beforeDescend();
			visit(((SubExpression)expr).getLHS(),visitor);
			visitor.betweenDescend();
			visit(((SubExpression)expr).getRHS(),visitor);
			visitor.afterDescend();
		} 
		else if (expr instanceof DivExpression){
			visitor.visit((DivExpression)expr);
			visitor.beforeDescend();
			visit(((DivExpression)expr).getLHS(),visitor);
			visitor.betweenDescend();
			visit(((DivExpression)expr).getRHS(),visitor);
			visitor.afterDescend();
		} 
		else if (expr instanceof MulExpression){
			visitor.visit((MulExpression)expr);
			visitor.beforeDescend();
			visit(((MulExpression)expr).getLHS(),visitor);
			visitor.betweenDescend();
			visit(((MulExpression)expr).getRHS(),visitor);
			visitor.afterDescend();
		} 
		else if (expr instanceof ModExpression){
			visitor.visit((ModExpression)expr);
			visitor.beforeDescend();
			visit(((ModExpression)expr).getLHS(),visitor);
			visitor.betweenDescend();
			visit(((ModExpression)expr).getRHS(),visitor);
			visitor.afterDescend();
		} 
		else if (expr instanceof StringLiteral){
			visitor.visit((StringLiteral)expr);
		}
		else if (expr instanceof BooleanLiteral){
			visitor.visit((BooleanLiteral)expr);
		}
		else if (expr instanceof DecimalLiteral){
			visitor.visit((DecimalLiteral)expr);
		}
		else if (expr instanceof SingleLiteral){
			visitor.visit((SingleLiteral)expr);
		}
		else if (expr instanceof BinaryLiteral){
			visitor.visit((BinaryLiteral)expr);
		}
		else if (expr instanceof DoubleLiteral){
			visitor.visit((DoubleLiteral)expr);
		}
		else if (expr instanceof IntegralLiteral){
			visitor.visit((IntegralLiteral)expr);
		}
		else if (expr instanceof Int64Literal){
			visitor.visit((Int64Literal)expr);
		}
		else if (expr instanceof GuidLiteral){
			visitor.visit((GuidLiteral)expr);
		}
		else if (expr instanceof DateTimeLiteral){
			visitor.visit((DateTimeLiteral)expr);
		}
		else if (expr instanceof DateTimeOffsetLiteral){
			visitor.visit((DateTimeOffsetLiteral)expr);
		}
		else if (expr instanceof TimeLiteral){
			visitor.visit((TimeLiteral)expr);
		}
		else if (expr instanceof EntitySimpleProperty){
			visitor.visit((EntitySimpleProperty)expr);
		}
		else if (expr instanceof NullLiteral){
			visitor.visit((NullLiteral)expr);
		}
		else if (expr instanceof ParenExpression){
			visitor.visit((ParenExpression)expr);
			visitor.beforeDescend();
			visit(((ParenExpression)expr).getExpression(),visitor);
			visitor.afterDescend();
		} 
		else if (expr instanceof NotExpression){
			visitor.visit((NotExpression)expr);
			visitor.beforeDescend();
			visit(((NotExpression)expr).getExpression(),visitor);
			visitor.afterDescend();
		} 
		else if (expr instanceof NegateExpression){
			visitor.visit((NegateExpression)expr);
			visitor.beforeDescend();
			visit(((NegateExpression)expr).getExpression(),visitor);
			visitor.afterDescend();
		} 
		else if (expr instanceof CastExpression){
			visitor.visit((CastExpression)expr);
			visitor.beforeDescend();
			if (((CastExpression) expr).getExpression()!=null){
				visit(((CastExpression)expr).getExpression(),visitor);
				visitor.betweenDescend();
			}
			visitor.visit(((CastExpression)expr).getType());
			visitor.afterDescend();
		}
		else if (expr instanceof IsofExpression){
			visitor.visit((IsofExpression)expr);
			visitor.beforeDescend();
			if (((IsofExpression) expr).getExpression()!=null){
				visit(((IsofExpression)expr).getExpression(),visitor);
				visitor.betweenDescend();
			}
			visitor.visit(((IsofExpression)expr).getType());
			visitor.afterDescend();
		}
		else if (expr instanceof EndsWithMethodCallExpression){
			visitor.visit((EndsWithMethodCallExpression)expr);
			visitor.beforeDescend();
			visit(((EndsWithMethodCallExpression)expr).getTarget(),visitor);
			visitor.betweenDescend();
			visit(((EndsWithMethodCallExpression)expr).getValue(),visitor);
			visitor.afterDescend();
		}
		else if (expr instanceof StartsWithMethodCallExpression){
			visitor.visit((StartsWithMethodCallExpression)expr);
			visitor.beforeDescend();
			visit(((StartsWithMethodCallExpression)expr).getTarget(),visitor);
			visitor.betweenDescend();
			visit(((StartsWithMethodCallExpression)expr).getValue(),visitor);
			visitor.afterDescend();
		}
		else if (expr instanceof SubstringOfMethodCallExpression){
			visitor.visit((SubstringOfMethodCallExpression)expr);
			visitor.beforeDescend();
			visit(((SubstringOfMethodCallExpression)expr).getValue(),visitor);
			if (((SubstringOfMethodCallExpression) expr).getTarget()!=null){
				visitor.betweenDescend();
				visit(((SubstringOfMethodCallExpression)expr).getTarget(),visitor);
			}
			visitor.afterDescend();
		}
		else if (expr instanceof IndexOfMethodCallExpression){
			visitor.visit((IndexOfMethodCallExpression)expr);
			visitor.beforeDescend();
			visit(((IndexOfMethodCallExpression)expr).getTarget(),visitor);
			visitor.betweenDescend();
			visit(((IndexOfMethodCallExpression)expr).getValue(),visitor);
			visitor.afterDescend();
		}
		else if (expr instanceof ReplaceMethodCallExpression){
			visitor.visit((ReplaceMethodCallExpression)expr);
			visitor.beforeDescend();
			visit(((ReplaceMethodCallExpression)expr).getTarget(),visitor);
			visitor.betweenDescend();
			visit(((ReplaceMethodCallExpression)expr).getFind(),visitor);
			visitor.betweenDescend();
			visit(((ReplaceMethodCallExpression)expr).getReplace(),visitor);
			visitor.afterDescend();
		}
		else if (expr instanceof ToLowerMethodCallExpression){
			visitor.visit((ToLowerMethodCallExpression)expr);
			visitor.beforeDescend();
			visit(((ToLowerMethodCallExpression)expr).getTarget(),visitor);
			visitor.afterDescend();
		}
		else if (expr instanceof ToUpperMethodCallExpression){
			visitor.visit((ToUpperMethodCallExpression)expr);
			visitor.beforeDescend();
			visit(((ToUpperMethodCallExpression)expr).getTarget(),visitor);
			visitor.afterDescend();
		}
		else if (expr instanceof TrimMethodCallExpression){
			visitor.visit((TrimMethodCallExpression)expr);
			visitor.beforeDescend();
			visit(((TrimMethodCallExpression)expr).getTarget(),visitor);
			visitor.afterDescend();
		}
		else if (expr instanceof SubstringMethodCallExpression){
			visitor.visit((SubstringMethodCallExpression)expr);
			visitor.beforeDescend();
			visit(((SubstringMethodCallExpression)expr).getTarget(),visitor);
			visitor.betweenDescend();
			visit(((SubstringMethodCallExpression)expr).getStart(),visitor);
			if (((SubstringMethodCallExpression) expr).getLength()!=null){
				visitor.betweenDescend();
				visit(((SubstringMethodCallExpression)expr).getLength(),visitor);
			}
			visitor.afterDescend();
		}
		else if (expr instanceof ConcatMethodCallExpression){
			visitor.visit((ConcatMethodCallExpression)expr);
			visitor.beforeDescend();
			visit(((ConcatMethodCallExpression)expr).getLHS(),visitor);
			visitor.betweenDescend();
			visit(((ConcatMethodCallExpression)expr).getRHS(),visitor);
			visitor.afterDescend();
		}
		else if (expr instanceof LengthMethodCallExpression){
			visitor.visit((LengthMethodCallExpression)expr);
			visitor.beforeDescend();
			visit(((LengthMethodCallExpression)expr).getTarget(),visitor);
			visitor.afterDescend();
		}
		else if (expr instanceof YearMethodCallExpression){
			visitor.visit((YearMethodCallExpression)expr);
			visitor.beforeDescend();
			visit(((YearMethodCallExpression)expr).getTarget(),visitor);
			visitor.afterDescend();
		}
		else if (expr instanceof MonthMethodCallExpression){
			visitor.visit((MonthMethodCallExpression)expr);
			visitor.beforeDescend();
			visit(((MonthMethodCallExpression)expr).getTarget(),visitor);
			visitor.afterDescend();
		}
		else if (expr instanceof DayMethodCallExpression){
			visitor.visit((DayMethodCallExpression)expr);
			visitor.beforeDescend();
			visit(((DayMethodCallExpression)expr).getTarget(),visitor);
			visitor.afterDescend();
		}
		else if (expr instanceof HourMethodCallExpression){
			visitor.visit((HourMethodCallExpression)expr);
			visitor.beforeDescend();
			visit(((HourMethodCallExpression)expr).getTarget(),visitor);
			visitor.afterDescend();
		}
		else if (expr instanceof MinuteMethodCallExpression){
			visitor.visit((MinuteMethodCallExpression)expr);
			visitor.beforeDescend();
			visit(((MinuteMethodCallExpression)expr).getTarget(),visitor);
			visitor.afterDescend();
		}
		else if (expr instanceof SecondMethodCallExpression){
			visitor.visit((SecondMethodCallExpression)expr);
			visitor.beforeDescend();
			visit(((SecondMethodCallExpression)expr).getTarget(),visitor);
			visitor.afterDescend();
		}
		else if (expr instanceof RoundMethodCallExpression){
			visitor.visit((RoundMethodCallExpression)expr);
			visitor.beforeDescend();
			visit(((RoundMethodCallExpression)expr).getTarget(),visitor);
			visitor.afterDescend();
		}
		else if (expr instanceof CeilingMethodCallExpression){
			visitor.visit((CeilingMethodCallExpression)expr);
			visitor.beforeDescend();
			visit(((CeilingMethodCallExpression)expr).getTarget(),visitor);
			visitor.afterDescend();
		}
		else if (expr instanceof FloorMethodCallExpression){
			visitor.visit((FloorMethodCallExpression)expr);
			visitor.beforeDescend();
			visit(((FloorMethodCallExpression)expr).getTarget(),visitor);
			visitor.afterDescend();
		}
		else if (expr instanceof OrderByExpression){
			visitor.visit((OrderByExpression)expr);
			visitor.beforeDescend();
			visit(((OrderByExpression)expr).getExpression(),visitor);
			visitor.betweenDescend();
			visitor.visit(((OrderByExpression)expr).isAscending()?"asc":"desc");
			visitor.afterDescend();
		}
		else {
			throw new RuntimeException("Unsupported: " + expr);
		}
	
	}
	
}
