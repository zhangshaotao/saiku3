package org.saiku.query.mdx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.olap4j.mdx.CallNode;
import org.olap4j.mdx.HierarchyNode;
import org.olap4j.mdx.LiteralNode;
import org.olap4j.mdx.ParseTreeNode;
import org.olap4j.mdx.Syntax;
import org.olap4j.mdx.parser.MdxParser;
import org.olap4j.metadata.Hierarchy;

public class NameFilter extends AbstractFilterFunction
{
  private String operator = " = ";
  private String op;
  private List<String> filterExpression = new ArrayList();
  private IFilterFunction.MdxFunctionType type;
  private Hierarchy hierarchy;
  
  public NameFilter(Hierarchy hierarchy, String... matchingExpression)
  {
    List<String> expressions = Arrays.asList(matchingExpression);
    this.filterExpression.addAll(expressions);
    this.hierarchy = hierarchy;
    this.type = IFilterFunction.MdxFunctionType.Filter;
  }
  
  public NameFilter(Hierarchy hierarchy, List<String> matchingExpression)
  {
    this.hierarchy = hierarchy;
    this.filterExpression.addAll(matchingExpression);
    this.type = IFilterFunction.MdxFunctionType.Filter;
  }
  
  public NameFilter(Hierarchy hierarchy, List<String> matchingExpression, String operator)
  {
    this.hierarchy = hierarchy;
    this.filterExpression.addAll(matchingExpression);
    this.type = IFilterFunction.MdxFunctionType.Filter;
    this.op = operator;
    if ((operator != null) && (operator.equals("NOTEQUAL"))) {
      this.operator = " <> ";
    }
  }
  
  public List<ParseTreeNode> getArguments(MdxParser parser)
  {
    List<ParseTreeNode> filters = new ArrayList();
    List<ParseTreeNode> arguments = new ArrayList();
    ParseTreeNode h = new HierarchyNode(null, this.hierarchy);
    for (int i = 0; i < this.filterExpression.size(); i++)
    {
      String filter = (String)this.filterExpression.get(i);
      
      String o = this.operator;
      if ((this.filterExpression.size() > 1) && (i == 0)) {
        o = " = ";
      }
      ParseTreeNode filterExp = LiteralNode.createString(null, filter);
      CallNode currentMemberNode = new CallNode(null, "CurrentMember", Syntax.Property, new ParseTreeNode[] { h });
      
      CallNode currentMemberNameNode = new CallNode(null, "Name", Syntax.Property, new ParseTreeNode[] { currentMemberNode });
      
      CallNode filterNode = new CallNode(null, o, Syntax.Infix, new ParseTreeNode[] { currentMemberNameNode, filterExp });
      
      filters.add(filterNode);
    }
    if (filters.size() == 1)
    {
      arguments.addAll(filters);
    }
    else if (filters.size() > 1)
    {
      ParseTreeNode allfilter = (ParseTreeNode)filters.get(0);
      for (int i = 1; i < filters.size(); i++) {
        allfilter = new CallNode(null, " OR ", Syntax.Infix, new ParseTreeNode[] { allfilter, (ParseTreeNode)filters.get(i) });
      }
      arguments.add(allfilter);
    }
    return arguments;
  }
  
  public IFilterFunction.MdxFunctionType getFunctionType()
  {
    return this.type;
  }
  
  public List<String> getFilterExpression()
  {
    return this.filterExpression;
  }
  
  public Hierarchy getHierarchy()
  {
    return this.hierarchy;
  }
  
  public String getOp()
  {
    return this.op;
  }
}
