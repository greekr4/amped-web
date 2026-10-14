0 : {
		stop();
		changeFace(2);
		fadeinAnimation( 115.0 , 115.0 , 105.0 , 8 , 0, 0.2) ;
		ignoreAttack(1400);
		bind(1400);
		makeShapeModel("/effect/lcharge.oa",0,0.3,0,0,1,0);
		makeShapeModel("/effect/elec.oa",0,0.4,-0.3,0,1,0);
	}
300 : {
		fadeinAnimation( 117.0 , 118.0 , 117.0 , 4 , 1, 0.1) ;
		makeShapeModel("/effect/flash.oa",0,0.3,0.3,0,1,0);
	}
400 : {
		attack("salvo");
		makeShapeModel("/effect/spark.oa",0,0.2,0.4,0,0,1,1);
	}
520 : {
		attack("salvoL");
		attack("salvoR");
	}
640 : {
		attack("salvoLL");
		attack("salvoRR");
		makeShapeModel("/effect/elec.oa",0,0.3,0.3,0,1,0);
	}
900 : {
		startAnimation( 236.0 , 238.0 , 236.0 , 24 , 1) ;
	}
1400 : {
		recover();
	}
