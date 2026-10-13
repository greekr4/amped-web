0 : {
		fadeinAnimation( 290.0 , 290.0 , 290.0 , 3.0 , 0, 0.25) ;
		changeFace(1);
	}
250 : {
		startAnimation( 291.0 , 293.0 , 293.0 , 10 , 0) ;
		makeShapeModel("/effect/cold1.oa",0,0.1,0.3,0,1,0);
	}
350 : {
		attack("hailstone");
	}
500 : {
		stop();
		startAnimation( 293.0 , 294.0 , 294.0 , 3.5 , 0) ;
	}
800 : {
		recover();
	}
