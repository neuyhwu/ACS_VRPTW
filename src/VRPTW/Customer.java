package VRPTW;

public class Customer {
	int Number;//�ڵ�������
    int R;//�ڵ���������·�����
    int X, Y;//�ڵ��������
    int Begin, End, Service;//�ڵ㱻���ʵ�����ʱ�䣬����ʱ���Լ�����ʱ��
    int Demand;//�ڵ����������
    
    public Customer() {
    	this.Number=0;
    	this.R=0;
    	this.Begin =0;
    	this.End=0;
    	this.Service=0;
    	this.X=0;
    	this.Y=0;
    	this.Demand=0;
    }
    
    public Customer copy() {
    	Customer newCustomer = new Customer(); 
    	newCustomer.Number=this.Number;
    	newCustomer.R=this.R;
    	newCustomer.Begin =this.Begin;
    	newCustomer.End=this.End;
    	newCustomer.Service=this.Service;
    	newCustomer.X=this.X;
    	newCustomer.Y=this.Y;
    	newCustomer.Demand=this.Demand;
    	return newCustomer;
    }
}
