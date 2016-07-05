
var Hello = React.createClass({
  render: function(){
    return(
	<div className="Hello">
	<h1> Hello World! </h1>
	</div>
    );
  }

});

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
    return (
	<div className="currentVotesList">
	<h3> Current Votes: </h3>
	<div className="list-group">
		{this.state.votes.map(function(vote){
			return(<p>{vote.rest_id}</p>)
		})}
      </div>
	</div>)
  }

});

var RestaurantListItem = React.createClass({
  render: function(){
    return(<div className="list-group-item">
	   {this.props.name}
	   </div>);
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
	  <RestaurantListItem name={restaurant.name} key={restaurant.rest_id} />)
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
    return(
        <div className="popularResaturant">
        <h3> Popular restaurants for {this.dayofweek()}s : </h3>
	{this.state.data}
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
    <PopularRestaurant url="/api/popular-restaurant" />,
  document.getElementById('content-right'));
