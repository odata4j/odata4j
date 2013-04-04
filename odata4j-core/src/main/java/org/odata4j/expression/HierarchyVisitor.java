package org.odata4j.expression;

import org.odata4j.expression.OrderByExpression.Direction;

/**
 * visitNode - traverses the language object, only exists on HierarchyVisitor
 * visitThis - accepts a visitor on the language object - only exists on all the language objects
 * visit - is a visitor method. only exists on visitor classes
 */
public abstract class HierarchyVisitor implements ExpressionVisitor {
  private HierarchyVisitor delegate;

  public void setDelegate(HierarchyVisitor delegate) {
    this.delegate = delegate;
  }

  public abstract void visitNode(CommonExpression obj);

  public void visitNode(BinaryCommonExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getLHS());
      betweenDescend();
      visitNode(obj.getRHS());
      afterDescend();
    }
  }

  public void visitNode(BinaryBoolCommonExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getLHS());
      betweenDescend();
      visitNode(obj.getRHS());
      afterDescend();
    }
  }

  public void visitNode(BoolParenExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getExpression());
      afterDescend();
    }
  }

  public void visitNode(NegateExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getExpression());
      afterDescend();
    }
  }

  public void visitNode(NotExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getExpression());
      afterDescend();
    }
  }

  public void visitNode(ParenExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getExpression());
      afterDescend();
    }
  }

  public void visitNode(OrderByExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getExpression());
      betweenDescend();
      visit(obj.getDirection());
      afterDescend();
    }
  }

  public void visitNode(EndsWithMethodCallExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getTarget());
      betweenDescend();
      visitNode(obj.getValue());
      afterDescend();
    }
  }

  public void visitNode(StartsWithMethodCallExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getTarget());
      betweenDescend();
      visitNode(obj.getValue());
      afterDescend();
    }
  }

  public void visitNode(SubstringOfMethodCallExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getValue());
      if (obj.getTarget() != null) {
        betweenDescend();
        visitNode(obj.getTarget());
      }
      afterDescend();
    }
  }

  public void visitNode(CeilingMethodCallExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getTarget());
      afterDescend();
    }
  }

  public void visitNode(ConcatMethodCallExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getLHS());
      betweenDescend();
      visitNode(obj.getRHS());
      afterDescend();
    }
  }

  public void visitNode(DayMethodCallExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getTarget());
      afterDescend();
    }
  }

  public void visitNode(FloorMethodCallExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getTarget());
      afterDescend();
    }
  }

  public void visitNode(HourMethodCallExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getTarget());
      afterDescend();
    }
  }

  public void visitNode(IndexOfMethodCallExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getTarget());
      betweenDescend();
      visitNode(obj.getValue());
      afterDescend();
    }
  }

  public void visitNode(LengthMethodCallExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getTarget());
      afterDescend();
    }
  }

  public void visitNode(MinuteMethodCallExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getTarget());
      afterDescend();
    }
  }

  public void visitNode(MonthMethodCallExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getTarget());
      afterDescend();
    }
  }

  public void visitNode(ReplaceMethodCallExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getTarget());
      betweenDescend();
      visitNode(obj.getFind());
      betweenDescend();
      visitNode(obj.getReplace());
      afterDescend();
    }
  }

  public void visitNode(RoundMethodCallExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getTarget());
      afterDescend();
    }
  }

  public void visitNode(SecondMethodCallExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getTarget());
      afterDescend();
    }
  }

  public void visitNode(SubstringMethodCallExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
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
  }

  public void visitNode(ToLowerMethodCallExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getTarget());
      afterDescend();
    }
  }

  public void visitNode(ToUpperMethodCallExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getTarget());
      afterDescend();
    }
  }

  public void visitNode(TrimMethodCallExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getTarget());
      afterDescend();
    }
  }

  public void visitNode(YearMethodCallExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getTarget());
      afterDescend();
    }
  }

  public void visitNode(AggregateBoolFunction obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
      obj.visitThis(this);
      beforeDescend();
      visitNode(obj.getSource());
      betweenDescend();
      if (obj.getPredicate() != null) {
        visitNode(obj.getPredicate());
      }
      afterDescend();
    }
  }

  public void visitNode(IsofExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
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

  public void visitNode(CastExpression obj) {
    if (delegate != null) {
      delegate.visitNode(obj);
    } else {
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

  @Override
  public void beforeDescend() {
  }

  @Override
  public void afterDescend() {
  }

  @Override
  public void betweenDescend() {
  }

  @Override
  public void visit(String type) {
  }

  @Override
  public void visit(OrderByExpression expr) {
  }

  @Override
  public void visit(Direction direction) {
  }

  @Override
  public void visit(AddExpression expr) {
  }

  @Override
  public void visit(AndExpression expr) {
  }

  @Override
  public void visit(BooleanLiteral expr) {
  }

  @Override
  public void visit(CastExpression expr) {
  }

  @Override
  public void visit(ConcatMethodCallExpression expr) {
  }

  @Override
  public void visit(DateTimeLiteral expr) {
  }

  @Override
  public void visit(DateTimeOffsetLiteral expr) {
  }

  @Override
  public void visit(DecimalLiteral expr) {
  }

  @Override
  public void visit(DivExpression expr) {
  }

  @Override
  public void visit(EndsWithMethodCallExpression expr) {
  }

  @Override
  public void visit(EntitySimpleProperty expr) {
  }

  @Override
  public void visit(EqExpression expr) {
  }

  @Override
  public void visit(GeExpression expr) {
  }

  @Override
  public void visit(GtExpression expr) {
  }

  @Override
  public void visit(GuidLiteral expr) {
  }

  @Override
  public void visit(BinaryLiteral expr) {
  }

  @Override
  public void visit(ByteLiteral expr) {
  }

  @Override
  public void visit(SByteLiteral expr) {
  }

  @Override
  public void visit(IndexOfMethodCallExpression expr) {
  }

  @Override
  public void visit(SingleLiteral expr) {
  }

  @Override
  public void visit(DoubleLiteral expr) {
  }

  @Override
  public void visit(IntegralLiteral expr) {
  }

  @Override
  public void visit(Int64Literal expr) {
  }

  @Override
  public void visit(IsofExpression expr) {
  }

  @Override
  public void visit(LeExpression expr) {
  }

  @Override
  public void visit(LengthMethodCallExpression expr) {
  }

  @Override
  public void visit(LtExpression expr) {
  }

  @Override
  public void visit(ModExpression expr) {
  }

  @Override
  public void visit(MulExpression expr) {
  }

  @Override
  public void visit(NeExpression expr) {
  }

  @Override
  public void visit(NegateExpression expr) {
  }

  @Override
  public void visit(NotExpression expr) {
  }

  @Override
  public void visit(NullLiteral expr) {
  }

  @Override
  public void visit(OrExpression expr) {
  }

  @Override
  public void visit(ParenExpression expr) {
  }

  @Override
  public void visit(BoolParenExpression expr) {
  }

  @Override
  public void visit(ReplaceMethodCallExpression expr) {
  }

  @Override
  public void visit(StartsWithMethodCallExpression expr) {
  }

  @Override
  public void visit(StringLiteral expr) {
  }

  @Override
  public void visit(SubExpression expr) {
  }

  @Override
  public void visit(SubstringMethodCallExpression expr) {
  }

  @Override
  public void visit(SubstringOfMethodCallExpression expr) {
  }

  @Override
  public void visit(TimeLiteral expr) {
  }

  @Override
  public void visit(ToLowerMethodCallExpression expr) {
  }

  @Override
  public void visit(ToUpperMethodCallExpression expr) {
  }

  @Override
  public void visit(TrimMethodCallExpression expr) {
  }

  @Override
  public void visit(YearMethodCallExpression expr) {
  }

  @Override
  public void visit(MonthMethodCallExpression expr) {
  }

  @Override
  public void visit(DayMethodCallExpression expr) {
  }

  @Override
  public void visit(HourMethodCallExpression expr) {
  }

  @Override
  public void visit(MinuteMethodCallExpression expr) {
  }

  @Override
  public void visit(SecondMethodCallExpression expr) {
  }

  @Override
  public void visit(RoundMethodCallExpression expr) {
  }

  @Override
  public void visit(FloorMethodCallExpression expr) {
  }

  @Override
  public void visit(CeilingMethodCallExpression expr) {
  }

  @Override
  public void visit(AggregateAnyFunction expr) {
  }

  @Override
  public void visit(AggregateAllFunction expr) {
  }
}
