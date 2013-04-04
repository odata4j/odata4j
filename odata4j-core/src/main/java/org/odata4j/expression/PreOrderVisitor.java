package org.odata4j.expression;

import org.odata4j.expression.Expression.AddExpressionImpl;
import org.odata4j.expression.Expression.AggregateAllFunctionImpl;
import org.odata4j.expression.Expression.AggregateAnyFunctionImpl;
import org.odata4j.expression.Expression.AggregateBoolFunctionImpl;
import org.odata4j.expression.Expression.AndExpressionImpl;
import org.odata4j.expression.Expression.BoolParenExpressionImpl;
import org.odata4j.expression.Expression.CastExpressionImpl;
import org.odata4j.expression.Expression.CeilingMethodCallExpressionImpl;
import org.odata4j.expression.Expression.ConcatMethodCallExpressionImpl;
import org.odata4j.expression.Expression.DayMethodCallExpressionImpl;
import org.odata4j.expression.Expression.DivExpressionImpl;
import org.odata4j.expression.Expression.EndsWithMethodCallExpressionImpl;
import org.odata4j.expression.Expression.EqExpressionImpl;
import org.odata4j.expression.Expression.FloorMethodCallExpressionImpl;
import org.odata4j.expression.Expression.GeExpressionImpl;
import org.odata4j.expression.Expression.GtExpressionImpl;
import org.odata4j.expression.Expression.HourMethodCallExpressionImpl;
import org.odata4j.expression.Expression.IndexOfMethodCallExpressionImpl;
import org.odata4j.expression.Expression.IsofExpressionImpl;
import org.odata4j.expression.Expression.LeExpressionImpl;
import org.odata4j.expression.Expression.LengthMethodCallExpressionImpl;
import org.odata4j.expression.Expression.LtExpressionImpl;
import org.odata4j.expression.Expression.MinuteMethodCallExpressionImpl;
import org.odata4j.expression.Expression.ModExpressionImpl;
import org.odata4j.expression.Expression.MonthMethodCallExpressionImpl;
import org.odata4j.expression.Expression.MulExpressionImpl;
import org.odata4j.expression.Expression.NeExpressionImpl;
import org.odata4j.expression.Expression.NegateExpressionImpl;
import org.odata4j.expression.Expression.NotExpressionImpl;
import org.odata4j.expression.Expression.OrExpressionImpl;
import org.odata4j.expression.Expression.OrderByExpressionImpl;
import org.odata4j.expression.Expression.ParenExpressionImpl;
import org.odata4j.expression.Expression.ReplaceMethodCallExpressionImpl;
import org.odata4j.expression.Expression.RoundMethodCallExpressionImpl;
import org.odata4j.expression.Expression.SecondMethodCallExpressionImpl;
import org.odata4j.expression.Expression.StartsWithMethodCallExpressionImpl;
import org.odata4j.expression.Expression.SubExpressionImpl;
import org.odata4j.expression.Expression.SubstringMethodCallExpressionImpl;
import org.odata4j.expression.Expression.SubstringOfMethodCallExpressionImpl;
import org.odata4j.expression.Expression.ToLowerMethodCallExpressionImpl;
import org.odata4j.expression.Expression.ToUpperMethodCallExpressionImpl;
import org.odata4j.expression.Expression.TrimMethodCallExpressionImpl;
import org.odata4j.expression.Expression.YearMethodCallExpressionImpl;

public abstract class PreOrderVisitor implements ExpressionVisitor {
  public void visitNode(CommonExpression obj) {
    if (obj instanceof AddExpressionImpl) {
      visitNode((AddExpression) obj);
    } else if (obj instanceof AggregateAllFunctionImpl) {
      visitNode((AggregateAllFunction) obj);
    } else if (obj instanceof AggregateAnyFunctionImpl) {
      visitNode((AggregateAnyFunction) obj);
    } else if (obj instanceof AggregateBoolFunctionImpl) {
      visitNode((AggregateBoolFunction) obj);
    } else if (obj instanceof AndExpressionImpl) {
      visitNode((AndExpression) obj);
    } else if (obj instanceof BoolParenExpressionImpl) {
      visitNode((BoolParenExpression) obj);
    } else if (obj instanceof CastExpressionImpl) {
      visitNode((CastExpression) obj);
    } else if (obj instanceof CeilingMethodCallExpressionImpl) {
      visitNode((CeilingMethodCallExpression) obj);
    } else if (obj instanceof ConcatMethodCallExpressionImpl) {
      visitNode((ConcatMethodCallExpression) obj);
    } else if (obj instanceof DayMethodCallExpressionImpl) {
      visitNode((DayMethodCallExpression) obj);
    } else if (obj instanceof DivExpressionImpl) {
      visitNode((DivExpression) obj);
    } else if (obj instanceof EndsWithMethodCallExpressionImpl) {
      visitNode((EndsWithMethodCallExpression) obj);
    } else if (obj instanceof EqExpressionImpl) {
      visitNode((EqExpression) obj);
    } else if (obj instanceof FloorMethodCallExpressionImpl) {
      visitNode((FloorMethodCallExpression) obj);
    } else if (obj instanceof GeExpressionImpl) {
      visitNode((GeExpression) obj);
    } else if (obj instanceof GtExpressionImpl) {
      visitNode((GtExpression) obj);
    } else if (obj instanceof HourMethodCallExpressionImpl) {
      visitNode((HourMethodCallExpression) obj);
    } else if (obj instanceof IndexOfMethodCallExpressionImpl) {
      visitNode((IndexOfMethodCallExpression) obj);
    } else if (obj instanceof IsofExpressionImpl) {
      visitNode((IsofExpression) obj);
    } else if (obj instanceof LeExpressionImpl) {
      visitNode((LeExpression) obj);
    } else if (obj instanceof LengthMethodCallExpressionImpl) {
      visitNode((LengthMethodCallExpression) obj);
    } else if (obj instanceof LtExpressionImpl) {
      visitNode((LtExpression) obj);
    } else if (obj instanceof MinuteMethodCallExpressionImpl) {
      visitNode((MinuteMethodCallExpression) obj);
    } else if (obj instanceof ModExpressionImpl) {
      visitNode((ModExpression) obj);
    } else if (obj instanceof MonthMethodCallExpressionImpl) {
      visitNode((MonthMethodCallExpression) obj);
    } else if (obj instanceof MulExpressionImpl) {
      visitNode((MulExpression) obj);
    } else if (obj instanceof NeExpressionImpl) {
      visitNode((NeExpression) obj);
    } else if (obj instanceof NegateExpressionImpl) {
      visitNode((NegateExpression) obj);
    } else if (obj instanceof NotExpressionImpl) {
      visitNode((NotExpression) obj);
    } else if (obj instanceof OrderByExpressionImpl) {
      visitNode((OrderByExpression) obj);
    } else if (obj instanceof OrExpressionImpl) {
      visitNode((OrExpression) obj);
    } else if (obj instanceof ParenExpressionImpl) {
      visitNode((ParenExpression) obj);
    } else if (obj instanceof ReplaceMethodCallExpressionImpl) {
      visitNode((ReplaceMethodCallExpression) obj);
    } else if (obj instanceof RoundMethodCallExpressionImpl) {
      visitNode((RoundMethodCallExpression) obj);
    } else if (obj instanceof SecondMethodCallExpressionImpl) {
      visitNode((SecondMethodCallExpression) obj);
    } else if (obj instanceof StartsWithMethodCallExpressionImpl) {
      visitNode((StartsWithMethodCallExpression) obj);
    } else if (obj instanceof SubExpressionImpl) {
      visitNode((SubExpression) obj);
    } else if (obj instanceof SubstringMethodCallExpressionImpl) {
      visitNode((SubstringMethodCallExpression) obj);
    } else if (obj instanceof SubstringOfMethodCallExpressionImpl) {
      visitNode((SubstringOfMethodCallExpression) obj);
    } else if (obj instanceof ToLowerMethodCallExpressionImpl) {
      visitNode((ToLowerMethodCallExpression) obj);
    } else if (obj instanceof ToUpperMethodCallExpressionImpl) {
      visitNode((ToUpperMethodCallExpression) obj);
    } else if (obj instanceof TrimMethodCallExpressionImpl) {
      visitNode((TrimMethodCallExpression) obj);
    } else if (obj instanceof YearMethodCallExpressionImpl) {
      visitNode((YearMethodCallExpression) obj);
    } else {
      // literals are handled here.
      obj.visitThis(this);
    }
  }

  public void visitNode(BinaryCommonExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getLHS());
    betweenDescend();
    visitNode(obj.getRHS());
    afterDescend();
  }

  public void visitNode(BinaryBoolCommonExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getLHS());
    betweenDescend();
    visitNode(obj.getRHS());
    afterDescend();
  }

  public void visitNode(BoolParenExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getExpression());
    afterDescend();
  }

  public void visitNode(NegateExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getExpression());
    afterDescend();
  }

  public void visitNode(NotExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getExpression());
    afterDescend();
  }

  public void visitNode(ParenExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getExpression());
    afterDescend();
  }

  public void visitNode(OrderByExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getExpression());
    betweenDescend();
    visit(obj.getDirection());
    afterDescend();
  }

  public void visitNode(EndsWithMethodCallExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getTarget());
    betweenDescend();
    visitNode(obj.getValue());
    afterDescend();
  }

  public void visitNode(StartsWithMethodCallExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getTarget());
    betweenDescend();
    visitNode(obj.getValue());
    afterDescend();
  }

  public void visitNode(SubstringOfMethodCallExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getValue());
    if (obj.getTarget() != null) {
      betweenDescend();
      visitNode(obj.getTarget());
    }
    afterDescend();
  }

  public void visitNode(CeilingMethodCallExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getTarget());
    afterDescend();
  }

  public void visitNode(ConcatMethodCallExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getLHS());
    betweenDescend();
    visitNode(obj.getRHS());
    afterDescend();
  }

  public void visitNode(DayMethodCallExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getTarget());
    afterDescend();
  }

  public void visitNode(FloorMethodCallExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getTarget());
    afterDescend();
  }

  public void visitNode(HourMethodCallExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getTarget());
    afterDescend();
  }

  public void visitNode(IndexOfMethodCallExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getTarget());
    betweenDescend();
    visitNode(obj.getValue());
    afterDescend();
  }

  public void visitNode(LengthMethodCallExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getTarget());
    afterDescend();
  }

  public void visitNode(MinuteMethodCallExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getTarget());
    afterDescend();
  }

  public void visitNode(MonthMethodCallExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getTarget());
    afterDescend();
  }

  public void visitNode(ReplaceMethodCallExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getTarget());
    betweenDescend();
    visitNode(obj.getFind());
    betweenDescend();
    visitNode(obj.getReplace());
    afterDescend();
  }

  public void visitNode(RoundMethodCallExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getTarget());
    afterDescend();
  }

  public void visitNode(SecondMethodCallExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getTarget());
    afterDescend();
  }

  public void visitNode(SubstringMethodCallExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getTarget());
    betweenDescend();
    visitNode(obj.getStart());
    if (obj.getLength() != null) {
      betweenDescend();
      visitNode(obj.getLength());
    }
    afterDescend();
  }

  public void visitNode(ToLowerMethodCallExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getTarget());
    afterDescend();
  }

  public void visitNode(ToUpperMethodCallExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getTarget());
    afterDescend();
  }

  public void visitNode(TrimMethodCallExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getTarget());
    afterDescend();
  }

  public void visitNode(YearMethodCallExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getTarget());
    afterDescend();
  }

  public void visitNode(AggregateBoolFunction obj) {
    obj.visitThis(this);
    beforeDescend();
    visitNode(obj.getSource());
    betweenDescend();
    if (obj.getPredicate() != null) {
      visitNode(obj.getPredicate());
    }
    afterDescend();
  }

  public void visitNode(IsofExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    if (obj.getExpression() != null) {
      visitNode(obj.getExpression());
      betweenDescend();
    }
    visit(obj.getType());
    afterDescend();
  }

  public void visitNode(CastExpression obj) {
    obj.visitThis(this);
    beforeDescend();
    if (obj.getExpression() != null) {
      visitNode(obj.getExpression());
      betweenDescend();
    }
    visit(obj.getType());
    afterDescend();
  }
}