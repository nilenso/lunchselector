var CurrentVotes = React.createClass({
	getInitialState: function(){
		return {votes: []};
	},
	loadVotesFromServer: function() {
    $.ajax({
      url: this.props.url,
      dataType: 'json',
      cache: false,
      success: function(data) {
        this.setState({votes: data});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  componentDidMount: function() {
    this.loadVotesFromServer();
  },

  render: function(){
    var votes = this.state.votes.map(function(vote){
      return(<RestaurantListItem name={vote.name} id={vote.rest_id} votes={vote.votes} />)});
    return (
	<div className="currentVotesList">
	<h3> Current Votes: </h3>
	<div className="list-group">
	{votes}
      </div>
	</div>)
  }

});

var RestaurantListItem = React.createClass({
  submitvote: function(){
    var payload = {"votes": this.props.id};
    $.ajax({
      type: "POST",
      url: '/api/vote',
      dataType: 'json',
      contentType: 'application/json;charset=utf-8',
      data: JSON.stringify(payload),
      cache: false,
      success: function(data) {
        this.setState({voted: true});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  render: function(){
    return(<button onClick={this.submitvote} type="button " className="list-group-item">
	   <span className="badge">{this.props.votes}</span>
	   {this.props.name}
	   </button>);
  }
});


var RestaurantList = React.createClass({
  loadCommentsFromServer: function() {
    $.ajax({
      url: this.props.url,
      dataType: 'json',
      cache: false,
      success: function(data) {
        this.setState({data: data});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },

  getInitialState: function() {
    return {data: []};
  },

  componentDidMount: function() {
    this.loadCommentsFromServer();
    setInterval(this.loadCommentsFromServer, 20000);
  },

  render: function(){
    var restaurantListNodes = this.state.data.map(function(restaurant){
      return(
	  <RestaurantListItem name={restaurant.name} id={restaurant.rest_id} />)
    });

    return (
	<div className="restaurantList">
	<h3> Restaurants: </h3>
	<div className="list-group">
	{restaurantListNodes}
      </div>

      </div>)
  }

});

var PopularRestaurant = React.createClass({
  loadRestaurantsFromServer: function() {
    $.ajax({
      url: this.props.url,
      dataType: 'json',
      cache: false,
      success: function(data) {
        this.setState({data: data});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },

  getInitialState: function() {
    return {data: []};
  },

  componentDidMount: function() {
    this.loadRestaurantsFromServer();

  },

  dayofweek: function(){
  	var today = moment();
  	var day = today.format("dddd");
  	return day;
  },


  render: function(){

    var voted_restaurants = this.state.data.map(function(vote){

      return (<RestaurantListItem name={vote.name}  /> )})
    return(
        <div className="popularResaturant">
        <h3> Popular restaurants for {this.dayofweek()}s : </h3>
	<div className="list-group" >
          {voted_restaurants}
        </div>
      </div>
    )
  }
});


ReactDOM.render(
    <CurrentVotes url="/api/current-votes" />,
  document.getElementById('current-votes'));

ReactDOM.render(
    <RestaurantList url="/api/restaurants" />,
  document.getElementById('content'));

ReactDOM.render(
    <PopularRestaurant url="/api/popular-restaurant" />, document.getElementById('popular')
)
