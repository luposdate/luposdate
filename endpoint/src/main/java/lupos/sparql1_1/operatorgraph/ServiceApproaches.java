package lupos.sparql1_1.operatorgraph;

public enum ServiceApproaches {
	No_Support(){
		@Override
		public Class<? extends ServiceGenerator> serviceGeneratorClass() {
			return ServiceGenerator.class;
		}
	}, Trivial_Approach(){
		@Override
		public Class<? extends ServiceGenerator> serviceGeneratorClass() {
			return ServiceGeneratorTrivialApproach.class;
		}
	}, Fetch_As_Needed(){
		@Override
		public Class<? extends ServiceGenerator> serviceGeneratorClass() {
			return ServiceGeneratorFetchAsNeeded.class;
		}
	}, Semijoin_Approach(){
		@Override
		public Class<? extends ServiceGenerator> serviceGeneratorClass() {
			return ServiceGeneratorSemiJoin.class;
		}
	}, Bitvector_Join_Approach(){
		@Override
		public Class<? extends ServiceGenerator> serviceGeneratorClass() {
			return ServiceGeneratorBitVectorJoin.class;
		}
	}, Join_At_Endpoint(){
		@Override
		public Class<? extends ServiceGenerator> serviceGeneratorClass() {
			return ServiceGeneratorJoinAtEndpoint.class;
		}
	};
	public abstract Class<? extends ServiceGenerator> serviceGeneratorClass();
	
	public void setup(){
		SPARQLCoreParserVisitorImplementation.serviceGeneratorClass = this.serviceGeneratorClass();
	}
}
