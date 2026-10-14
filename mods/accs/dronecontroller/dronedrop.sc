0 : {
		stop();
		changeFace(2);
		fadeinAnimation( 290.0 , 290.0 , 290.0 , 3.0 , 0, 0.2) ;
		ignoreAttack(1300);
		bind(1300);
		jump(3.5);
		makeShapeModel("/effect/lcharge.oa",0,0.3,0,0,1,0);
	}
300 : {
		startAnimation( 291.0 , 293.0 , 293.0 , 10 , 0) ;
		makeShapeModel("/effect/flash.oa",0,0.3,0.3,0,1,0);
	}
400 : {
		attack("drop");
		makeShapeModel("/effect/spark.oa",0,0.2,0.4,0,0,1,1);
	}
600 : {
		startAnimation( 291.0 , 293.0 , 293.0 , 10 , 0) ;
		attack("dropL");
		attack("dropR");
		makeShapeModel("/effect/elec.oa",0,0.3,0.3,0,1,0);
	}
800 : {
		startAnimation( 293.0 , 294.0 , 294.0 , 3.5 , 0) ;
	}
1300 : {
		recover();
	}
