package main.frame;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import main.frame.utils.*;

import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.*;

public class InnerTest {
    
    private int i;
    public InnerTest(String xmlFolder){
        i = 1;
    }
    
    protected class LoadDataAction 
    {
      public int getInt(){
      	return i;
      }
      public void setInt(int j){
      	i = j;
      }
      
      public void setInt(){
      	System.err.println(i);;
      }
    }

}



