0 : {
		stop();
		fadeinAnimation( 200.0 , 200.0 , 200.0 , 4.5 , 0, 0.2) ;
	}
200 : {
		move(0,1);
		makeShapeModel("/effect/cold1.oa",0.2,0.0,0.5,0,0,1);
		startAnimation( 200.0 , 201.0 , 201.0 , 6.0 , 0) ;
	}
340 : {
		attack("icehook");
	}
400 : {
		stop();
		startAnimation( 201.0 , 203.0 , 203.0 , 3.5 , 0) ;
		nextTech();
	}
700 : {
		recover();
	}
