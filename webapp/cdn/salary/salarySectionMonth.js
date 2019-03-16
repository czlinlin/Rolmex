function getSectionMonth(monthIndex){
	var now = new Date();//2019,10,2
	var year=now.getFullYear();
	var month=now.getMonth();
	var startDate="";
	var endDate="";
	
	switch(monthIndex){
		case -1:
			if(month==0){
				startDate=endDate=(year-1)+"-"+"12";
			}
			else{
				startDate=endDate=year+"-"+(month<10?"0":"")+month;
			}
			break;
		case 0:
			month=(month+1);
			startDate=endDate=year+"-"+(month<10?"0":"")+month;
			break;
		case -2:
			if(month==0){
				startDate=endDate=(year-1)+"-"+"11";
			}
			else if(month==1){
				startDate=endDate=(year-1)+"-"+"12";
			}
			else{
				month=(month-1);
				startDate=endDate=year+"-"+(month<10?"0":"")+month;
			}
			break;
		case -3:
			if(month==0){
				startDate=endDate=(year-1)+"-"+"10";
			}
			else if(month==1){
				startDate=endDate=(year-1)+"-"+"11";
			}
			else if(month==2){
				startDate=endDate=(year-1)+"-"+"12";
			}
			else{
				month=(month-2);
				startDate=endDate=year+"-"+(month<10?"0":"")+month;
			}
			break;
		case -4:
			if(month==0){
				startDate=endDate=(year-1)+"-"+"10";
			}
			else if(month==1){
				startDate=endDate=(year-1)+"-"+"11";
			}
			else if(month==2){
				startDate=endDate=(year-1)+"-"+"12";
			}
			else{
				month=(month-3);
				startDate=endDate=year+"-"+(month<10?"0":"")+month;
			}
			break;
		case -5:
			if(month==0){
				startDate=endDate=(year-1)+"-"+"09";
			}
			else if(month==1){
				startDate=endDate=(year-1)+"-"+"10";
			}
			else if(month==2){
				startDate=endDate=(year-1)+"-"+"11";
			}
			else if(month==3){
				startDate=endDate=(year-1)+"-"+"12";
			}
			else{
				month=(month-4);
				startDate=endDate=year+"-"+(month<10?"0":"")+month;
			}
			break;
		case -6:
			if(month==0){
				startDate=endDate=(year-1)+"-"+"08";
			}
			else if(month==1){
				startDate=endDate=(year-1)+"-"+"09";
			}
			else if(month==2){
				startDate=endDate=(year-1)+"-"+"10";
			}
			else if(month==3){
				startDate=endDate=(year-1)+"-"+"11";
			}
			else if(month==4){
				startDate=endDate=(year-1)+"-"+"12";
			}
			else{
				month=(month-5);
				startDate=endDate=year+"-"+(month<10?"0":"")+month;
			}
			break;
	}
	$("#startDate").val(startDate);
	$("#endDate").val(endDate);
}