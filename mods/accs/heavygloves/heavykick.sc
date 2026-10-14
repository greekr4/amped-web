0 : {
		fadeinAnimation( 370.0 , 370.0 , 370.0 , 4.0 , 0, 0.24) ;
	}
240 : {
		startAnimation( 370.0 , 371.0 , 371.0 , 9.0 , 0) ;
	}
300 : {
		move(0,-4,5);
		makeShapeModel("/effect/shock.oa",0,-0.2,0.5,0,0,1,1);
		startAnimation( 372.0 , 374.0 , 372.0 , 24 , 1) ;
	}
380 : {
		attack("heavykick");
	}
500 : {
		stop();
	}
700 : {
		recover();
	}
